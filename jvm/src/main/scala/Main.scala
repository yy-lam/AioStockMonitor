import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.routing.{BalancingPool, RoundRobinPool}
import com.typesafe.config.ConfigFactory

object Main extends App {
  val system = ActorSystem.create("ActorSystem", ConfigFactory.load().getConfig("customConfig"))
  val master = system.actorOf(BalancingPool(10).props(Props[Classifier]), "master")
  val consumer = system.actorOf(SocketConsumer.props(9000, master), s"consumer")
}