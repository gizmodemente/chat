package com.gizmodemente.messages

import akka.actor.ActorRef

object ChatMessages {
  final case class NewMessage(message: String, chatName: String)
  final case class CreateChat(userId: String, chatName: String)
  final case class NewChat(chatName: String, chat: ActorRef)
  final case class RequestNewChat(chatName: String)
  final case class JoinChat(userId: String)
  final case class JoinToChat(chatName: String)
  final case class RefusedChat(chatName: String, reason: String)
  final case class ChatMessage(userId: String, message: String, chatName: String)
  final case class LeaveChat(userId: String)
  final case class Joined(chatName: String)
  final case class ListChats()
  final case class ListUsers()
  final case class CurrentChats(chats: List[String])
  final case class RemoveChat(chatName: String)
  final case class CreateUser(userId: String)
  final case class GetChatRef(chatName: String)
  final case class UserLeft(userId: String)
  final case class ChatLeft(chatName: String)
}
