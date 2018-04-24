package com.gizmodemente.actors.user

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.gizmodemente.messages.ChatMessages._

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object UserActor {
  def props(userId: String): Props = Props(new UserActor(userId))
}

class UserActor(userId: String) extends Actor with ActorLogging{

  val chatsJoined: mutable.SortedMap[String, String] = mutable.SortedMap[String, String]()

  override def receive: Receive = {
    case ChatMessage(userName, message, chatName) =>
      // In first approach only prints message with chat name
      println(if (chatsJoined.keySet.contains(chatName)) s"[$userId] $chatName"
        else s"[$userId] Unknown chat - $chatName")
      println(s"[$userId] $userName : $message")
    case RequestNewChat(chatName) => log.info("Requesting a new chat")
      context.parent ! CreateChat(userId, chatName)
    case NewChat(chatName, chat) =>
      log.info("Create Chat -> {}", chatName)
      chatsJoined += chatName -> chat
      context.actorSelection(chat) ! JoinChat(userId)
    case Joined(chatName) =>
      log.info("Join to Chat -> {}", chatName)
      chatsJoined += chatName -> sender().path.toStringWithoutAddress
    case RefusedChat(chatName, reason) =>
      log.info("Chat {} refused to join for: {}", chatName, reason)
    case NewMessage(message, chatName) =>
      log.info("Sending message from user {} to chat {}", userId, chatName)
      if(chatsJoined.isDefinedAt(chatName)) {
        context.actorSelection(chatsJoined(chatName)) ! ChatMessage(userId, message, chatName)
      } else log.error("User {} is not joined to Chat {}", userId, chatName)
    case JoinToChat(chatName) => log.info("Trying to join chat {}", chatName)
      implicit val timeout: Timeout = Timeout(1 seconds)
      val future = context.parent ? GetChatRef(chatName)
      context.actorSelection(Await.result(future.mapTo[String], timeout.duration)) ! JoinChat(userId)
    case ChatLeft(chatName) => log.info("Removing chat {} for user {}", chatName, userId)
      chatsJoined remove chatName
    case _ => log.error("Unexpected Message")
  }
}
