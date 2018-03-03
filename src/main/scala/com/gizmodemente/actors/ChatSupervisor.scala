package com.gizmodemente.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.gizmodemente.actors.chat.ChatActor
import com.gizmodemente.messages.ChatMessages.CreateChat

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
    case CreateChat(userId, chatName) =>
      context.actorOf(ChatActor.props(chatName))
  }
}
