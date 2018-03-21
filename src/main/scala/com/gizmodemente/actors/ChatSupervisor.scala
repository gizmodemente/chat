package com.gizmodemente.actors

import java.util.concurrent.TimeoutException

import akka.actor.SupervisorStrategy.{Escalate, Resume}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props}
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.actors.user.UserActor
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

object ChatSupervisor {
  def props(): Props = Props(new ChatSupervisor)
}

class ChatSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Chat Application started")
  override def postStop(): Unit = log.info("Chat Application stopped")

  val connectedUsers: mutable.SortedMap[String, String] = mutable.SortedMap[String, String]()
  val activeChats: mutable.SortedMap[String, String] = mutable.SortedMap[String, String]()

  override def aroundPreStart(): Unit = {
    log.info("Hello, i'm {} and you cand find me in {}", self.toString(), self.path.toStringWithoutAddress)
  }

  override def receive: Receive = {
    case CreateChat(userId, chatName) => log.info("Creating chat {} by user {}", chatName, userId)
      activeChats += (chatName -> context.actorOf(ChatActor.props(chatName)).path.toStringWithoutAddress)
      sender() ! NewChat(chatName, activeChats(chatName))
    case ListChats() => log.info("Listing all chats")
      sender() ! CurrentChats(activeChats.keySet.toList)
    case RemoveChat(chatName) => log.info("Removing chat {}", chatName)
      if(activeChats.isDefinedAt(chatName)) {
        log.info("Chat {} exists and will be removed", chatName)
        context.actorSelection(activeChats(chatName)) ! PoisonPill
        activeChats.remove(chatName)
      } else log.info("Chat {} doesn't exists", chatName)
    case CreateUser(userId) => log.info("Creating user actor for {}", userId)
      val newUser: ActorRef = context.actorOf(UserActor.props(userId), "user-" + generateActorName)
//      context.actorSelection("user-*") ! ChatMessage("system", "Connected user " + userId, "general")
      connectedUsers += userId -> newUser.path.toStringWithoutAddress
      sender() ! newUser.path.toStringWithoutAddress
    case GetChatRef(chatName) => log.info("Getting chat reference for {}", chatName)
      if(activeChats isDefinedAt chatName)
        sender() ! activeChats(chatName)
      else log.error("Chat {} doesn't exists", chatName)
    case ListUsers() => log.info("Listing all users")
      sender() ! connectedUsers.keySet.toList
    case UserLeft(userId) => log.info("User {} disconnect", userId)
      if(connectedUsers.isDefinedAt(userId)) {
        context.actorSelection(connectedUsers(userId)) ! PoisonPill
        connectedUsers remove userId
      } else log.error("Can't disconnect user {} because is not registered", userId)
    case _ => log.error("Unexpected Message")
  }

  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: TimeoutException => Resume
      case _: Exception => Escalate
    }

  def generateActorName: String = {
    val sb: mutable.StringBuilder = new mutable.StringBuilder
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

    for(i <- 1 to 8) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString()
  }

}
