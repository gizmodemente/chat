package com.gizmodemente

import akka.actor.ActorSystem
import akka.management.AkkaManagement
import com.gizmodemente.actors.ChatSupervisor
import com.gizmodemente.actors.cluster.ClusterListener
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object ChatApplication {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("chat-system")

    try {

      AkkaManagement(system).start()

      startup(Seq("2551", "2552"))

      system.actorOf(ChatSupervisor.props() , "chat-supervisor")
      system.actorOf(ClusterListener.props() , "cluster-listener")

      StdIn.readLine()

    } finally {
      system.terminate()
    }
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString(s"""
        akka.remote.netty.tcp.port=$port
        akka.remote.artery.canonical.port=$port
        """).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("chat-system", config)
      // Create an actor that handles cluster domain events
      system.actorOf(ClusterListener.props(), name = "clusterListener")
    }
  }
}
