package com.gizmodemente

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import akka.pattern.ask
import com.gizmodemente.actors.ChatSupervisor
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.actors.user.UserActor
import com.gizmodemente.messages.ChatMessages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class ChatTest extends TestKit(ActorSystem("chat-spec")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val user = system.actorOf(UserActor.props("TestUser"))
  val chat = system.actorOf(ChatActor.props("TestChat"))
  val supervisor = system.actorOf(ChatSupervisor.props())

  "A User" must {
    "Create a Chat" in {
      user ! CreateChat("TestUser", "TestChat")
      expectNoMessage(1.second)
    }

    "Receive a Chat Message" in {
      user ! ChatMessage("Friend", "Hello Friend", "TestChat")
      expectNoMessage(1.second)
    }

    "Join a Chat" in {
      chat ! JoinChat("TestChat")
      expectMsg(Joined("TestChat"))
    }
  }

  "A supervisor" must {
    "Create a chat" in {
      supervisor ! CreateChat("TestUser", "TestChat")
      expectMsgPF() {
        case NewChat("TestChat", _) => ()
      }
      supervisor ! CreateChat("TestUser", "TestChat2")
      expectMsgPF() {
        case NewChat("TestChat2", _) => ()
      }
      supervisor ! ListChats()
      expectMsg(CurrentChats(List("TestChat", "TestChat2")))
    }
    "Remove a chat" in {
      supervisor ! CreateChat("TestUser", "TestChat")
      expectMsgPF() {
        case NewChat("TestChat", _) => ()
      }
      supervisor ! RemoveChat("TestChat")
      expectNoMessage(1.second)
    }
    "Create a user that create a chat and send message to this chat" in {
      implicit val timeout = Timeout(1 seconds)
      val userActorFuture = supervisor ? CreateUser("TestUser")
      val userActor = Await.result(userActorFuture.mapTo[ActorRef], timeout.duration)
      userActor ! RequestNewChat("Test Chat")
      Thread.sleep(1000)
      val userActorFuture2 = supervisor ? CreateUser("TestUser2")
      val userActor2 = Await.result(userActorFuture2.mapTo[ActorRef], timeout.duration)
      Thread.sleep(1000)
      userActor2 ! JoinToChat("Test Chat")
      Thread.sleep(1000)
      userActor ! NewMessage("Hello Test", "Test Chat")
      Thread.sleep(1000)
    }
  }
}