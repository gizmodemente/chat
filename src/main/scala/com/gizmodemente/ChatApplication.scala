package com.gizmodemente

import akka.actor.ActorSystem
import com.gizmodemente.actors.ChatSupervisor

import scala.io.StdIn

class ChatApplication {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("chat-system")

    try {
      system.actorOf(ChatSupervisor.props() , "chat-supervisor")

      StdIn.readLine()

    } finally {
      system.terminate()
    }
  }
}
