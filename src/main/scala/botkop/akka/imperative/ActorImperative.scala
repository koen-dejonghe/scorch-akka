package botkop.akka.imperative

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

object ActorImperative {

  val system = ActorSystem("AskTestSystem")
  implicit val timeout: Timeout = Timeout(5 seconds)

  case class Variable(value: Double, fn: Option[ActorRef] = None) {
    var g: Double = 0

    def backward(og: Double): Unit = {
      g += og
      fn.foreach(f => f ! Backward(og))
    }

    def *(that: Variable): Variable = {
      val op = system.actorOf(MultiplyFunction.props(this, that))
      val f = op ? Forward
      Await.result(f, timeout.duration).asInstanceOf[Variable]
    }
  }

  case class MultiplyFunction(v1: Variable, v2: Variable) extends Actor {
    override def receive: Receive = {
      case Forward =>
        val result = Variable(v1.value * v2.value, Some(self))
        sender() ! result
      case Backward(og) =>
        val dv1 = v2.value * og
        val dv2 = v1.value * og
        v1.backward(dv1)
        v2.backward(dv2)
    }
  }

  object MultiplyFunction {
    def props(v1: Variable, v2: Variable): Props = Props(new MultiplyFunction(v1, v2))
  }

  case class Forward()
  case class Backward(og: Double)

}
