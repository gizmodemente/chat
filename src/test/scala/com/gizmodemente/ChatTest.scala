package com.gizmodemente

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.actors.user.UserActor
import com.gizmodemente.messages.ChatMessages._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class ChatTest extends TestKit(ActorSystem("chat-spec")) with ImplicitSender with WordSpecLike
  with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val user = system.actorOf(UserActor.props("TestUser"))
  val chat = system.actorOf(ChatActor.props("TestChat"))

  "A User" must {
    "Create a Chat" in {
      user ! CreateChat("TestUser", "TestChat")
      expectNoMessage(1.second)
    }

    "Receive a Chat Message" in {
      user ! ChatMessage("Friend", "Hello Friend")
      expectNoMessage(1.second)
    }

    "Join a Chat" in {
      chat ! JoinChat("TestChat")
      expectMsg(Joined("TestChat"))
    }
  }
}