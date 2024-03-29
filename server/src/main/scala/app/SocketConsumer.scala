package app

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}

import java.net.InetSocketAddress

object SocketConsumer {
  def props(port: Int, sink: ActorRef): Props = Props(new SocketConsumer(port, sink))
}

class SocketConsumer(port: Int, sink: ActorRef) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))

  // function that allows us to do some logic before the Actor is up
  override def preStart(): Unit = {
    log.info(s"Starting socket CONSUMER actor in port $port")
  }

  // function that allows us to do some logic once the Actor is down
  override def postStop(): Unit = {
    log.info("Stopped socket CONSUMER actor")
  }

  override def receive: Receive = {

    case _ @ Bound(localAddress) =>
      log.info(s"CONSUMER bound to: $localAddress")

    case CommandFailed(_: Bind) => context stop self

    case _ @ Connected(remote, local) =>
      log.info(s"CONSUMER is connected to: ($remote, $local)")
      val connection = sender()
      connection ! Register(self, keepOpenOnPeerClosed = true)

    case Received(message) =>
      for (combo <- message.decodeString("UTF8").split('\n')) sink ! combo

//    case PeerClosed     => context stop self

    //if the Actor receives a message that it doesn't understand, it sends a warning through the logger
    case x @ _ => log.warning("Something else is up. ---> " + x.toString)

  }
}