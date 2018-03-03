package com.gizmodemente.messages

import akka.actor.ActorRef

object ChatMessages {
  final case class NewMessage(message: String, chat: ActorRef)
  final case class CreateChat(userId: String, chatName: String)
  final case class JoinChat(chatName: String)
  final case class RefusedChat(chatName: String, reason: String)
  final case class ChatMessage(userId: String, message: String)
  final case class LeaveChat(userId: String)
  final case class Joined(chatName: String)
}
