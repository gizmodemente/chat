package com.gizmodemente.actors.chat

import akka.actor.{Actor, ActorLogging, Props}
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable

object ChatActor {
  def props(chatName: String): Props = Props(new ChatActor(chatName))
}

class ChatActor(chatName: String) extends Actor with ActorLogging{

  val users: mutable.SortedMap[String, String] = scala.collection.mutable.SortedMap[String, String]()

  val messages: mutable.SortedMap[String, String] = scala.collection.mutable.SortedMap[String, String]()

  override def receive: Receive = {
    case JoinChat(userId) => log.info("User {} join to channel", userId)
      users += userId -> sender().path.toStringWithoutAddress
      sender() ! Joined(chatName)
    case ChatMessage(userId, message, chat) =>
      log.info("Received Message from user {}", userId)
      if(chat.eq(chatName)) {
        messages + userId -> message
        users foreach (x => context.actorSelection(x._2) ! ChatMessage(userId, message.toUpperCase, chatName))
      } else log.error("Chat {} has received a message for chat {}", chatName, chat)
    case LeaveChat(userId) =>
      log.info("User {} has left the building", userId)
      context.actorSelection(users(userId)) ! ChatLeft(chatName)
      users remove userId
    case _ => log.error("Unexpected Message")
  }
}
