package com.gizmodemente.actors.user

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable

object UserActor {
  def props(userId: String): Props = Props(new UserActor(userId))
}

class UserActor(userId: String) extends Actor with ActorLogging{

  val chatsJoined = mutable.SortedMap[ActorRef, String]()

  override def receive: Receive = {
    case ChatMessage(userId, message) =>
      log.info("In first approach only prints message with chat name")
      println(chatsJoined.getOrElse(sender(), "Unknown chat"))
      println(userId + ": " + message)
    case CreateChat(userId, chatName) =>
      log.info("Create Chat -> {}", chatName)
      chatsJoined += sender() -> chatName
    case Joined(chatName) =>
      log.info("Join to Chat -> {}", chatName)
      chatsJoined += sender() -> chatName
    case RefusedChat(chatName, reason) =>
      log.info("Chat {} refused to join for: {}", chatName, reason)
//    case NewMessage(message) =>
    case _ => log.error("Unexpected Message")
  }
}
