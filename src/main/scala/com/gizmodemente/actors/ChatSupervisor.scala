package com.gizmodemente.actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.actors.user.UserActor
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable

object ChatSupervisor {
  def props(): Props = Props(new ChatSupervisor)
}

class ChatSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("Chat Application started")
  override def postStop(): Unit = log.info("Chat Application stopped")

  val connectedUsers = mutable.ListBuffer[ActorRef]()
  val activeChats = mutable.SortedMap[String, ActorRef]()

  override def receive: Receive = {
    case CreateChat(userId, chatName) => log.info("Creating chat {}", chatName)
      activeChats += (chatName -> context.actorOf(ChatActor.props(chatName)))
      sender() ! NewChat(chatName, activeChats(chatName))
    case ListChats() => log.info("Listing all chats")
      sender() ! CurrentChats(activeChats.keySet.toList)
    case RemoveChat(chatName) => log.info("Removing chat {}", chatName)
      if(activeChats.isDefinedAt(chatName)) {
        log.info("Chat {} exists and will be removed", chatName)
        activeChats(chatName) ! PoisonPill
        activeChats.remove(chatName)
      } else log.info("Chat {} doesn't exists", chatName)
    case CreateUser(userId) => log.info("Creating user actor for {}", userId)
      sender() ! context.actorOf(UserActor.props(userId))
  }
}
