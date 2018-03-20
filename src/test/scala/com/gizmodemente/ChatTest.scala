package com.gizmodemente

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.gizmodemente.actors.ChatSupervisor
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.actors.user.UserActor
import com.gizmodemente.messages.ChatMessages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
import scala.language.postfixOps

class ChatTest extends TestKit(ActorSystem("chat-spec")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val user: ActorRef = system.actorOf(UserActor.props("TestUser"))
  val chat: ActorRef = system.actorOf(ChatActor.props("TestChat"))
  val supervisor: ActorRef = system.actorOf(ChatSupervisor.props(), "supervisor")

  "A User" must {
    "Receive a chat message" in {
      user ! ChatMessage("Friend", "Hello Friend", "TestChat")
      expectNoMessage(1.second)
    }

    "Join a chat" in {
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
      implicit val timeout: Timeout = Timeout(1 seconds)
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
      supervisor ! UserLeft("TestUser")
      supervisor ! UserLeft("TestUser2")
    }
    "List connected users" in {
      val bound: Int = new Random().nextInt(150)

      for(x <- 1 to bound)
        supervisor ! CreateUser("TestUser" + x)

      implicit val timeout: Timeout = Timeout(1 seconds)
      val future = supervisor ? ListUsers()
      val users = Await.result(future.mapTo[List[String]], timeout.duration)

      users.size shouldBe bound

      for(user <- users)
        supervisor ! UserLeft(user)
    }
  }
}