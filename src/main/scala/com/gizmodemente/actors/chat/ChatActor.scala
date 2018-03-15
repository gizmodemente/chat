package com.gizmodemente.actors.chat

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.gizmodemente.messages.ChatMessages.{ChatMessage, JoinChat, Joined, LeaveChat}

object ChatActor {
  def props(chatName: String): Props = Props(new ChatActor(chatName))
}

class ChatActor(chatName: String) extends Actor with ActorLogging{

  val users = scala.collection.mutable.SortedMap[String, ActorRef]()

  val messages = scala.collection.mutable.SortedMap[String, String]()

  override def receive: Receive = {
    case JoinChat(userId) => log.info("User {} join to channel", userId)
      users += userId -> sender()
      sender() ! Joined(chatName)
    case ChatMessage(userId, message, chat) =>
      log.info("Received Message from user {}", userId)
      messages + userId -> message
      users foreach (x => x._2 ! ChatMessage(userId, message.toUpperCase, chatName))
    case LeaveChat(userId) =>
      log.info("User {} has left the building", userId)
      users remove userId
    case _ => log.error("Unexpected Message")
  }
}
