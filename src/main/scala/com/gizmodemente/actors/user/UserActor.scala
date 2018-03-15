package com.gizmodemente.actors.user

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable

object UserActor {
  def props(userId: String): Props = Props(new UserActor(userId))
}

class UserActor(userId: String) extends Actor with ActorLogging{

  val chatsJoined = mutable.SortedMap[String, ActorRef]()

  override def receive: Receive = {
    case ChatMessage(userName, message, chatName) =>
      // In first approach only prints message with chat name
      println(if (chatsJoined.keySet.contains(chatName)) chatName
      else "Unknown chat - " + chatName)
      println(userName + ": " + message)
    case RequestNewChat(chatName) => log.info("Requesting a new chat")
      context.parent ! CreateChat(userId, chatName)
    case NewChat(chatName, chat) =>
      log.info("Create Chat -> {}", chatName)
      chatsJoined += chatName -> chat
      chat ! JoinChat(userId)
    case Joined(chatName) =>
      log.info("Join to Chat -> {}", chatName)
      chatsJoined += chatName -> sender()
    case RefusedChat(chatName, reason) =>
      log.info("Chat {} refused to join for: {}", chatName, reason)
    case NewMessage(message, chatName) =>
      log.info("Sending message from user {} to chat {}", userId, chatName)
      if(chatsJoined.isDefinedAt(chatName)) {
        chatsJoined(chatName) ! ChatMessage(userId, message, chatName)
      } else log.error("User {} is not joined to Chat {}", userId, chatName)
    case _ => log.error("Unexpected Message")
  }
}
