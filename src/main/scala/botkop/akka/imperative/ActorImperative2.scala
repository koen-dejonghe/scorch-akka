package botkop.akka.imperative

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ActorImperative2 extends App {

  val system = ActorSystem(getClass.getName)

  def variable(d: Double): ActorRef = {
    system.actorOf(Variable.props(d))
  }

  case class Variable(d: Double) extends Actor {
    var g: Double = 0

    override def receive: Receive = {
      case Backward(og) =>
        g += og
        // context.parent ! Backward(og) ???
    }
  }

  object Variable {
    def props(d: Double): Props = Props(new Variable(d))
  }

  case class Backward(og: Double)

}

