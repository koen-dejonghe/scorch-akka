package botkop.akka.imperative

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

object ActorImperative extends App {

  val system = ActorSystem("AskTestSystem")
  implicit val timeout: Timeout = Timeout(5 seconds)

  val x = Variable(3)
  val y = Variable(5)
  val z = Variable(7)

  val a = x * y * z
  a.backward(1)

  println(a)
  println(x.g)
  println(y.g)
  println(z.g)

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

  class VarActor(value: Var, fn: Option[ActorRef] = None)
      extends Actor {

    override def receive: Receive = run(value)

    def run(v: Var): Receive = {
      case bog @ Backward(og) =>
        fn.foreach(f => f ! bog)
        context.become(run(v.copy(g = v.g + og)))
      case Result =>
        sender() ! v
    }
  }

  object VarActor {
    def props(value: Var, fn: Option[ActorRef] = None) =
      Props(new VarActor(value, fn))
  }

  class MultiplyFunction(v1: Variable, v2: Variable) extends Actor {
    val result = Variable(v1.value * v2.value, Some(self))

    override def receive: Receive = {
      case Forward =>
        sender() ! result
      case Backward(og) =>
        val dv1 = v2.value * og
        val dv2 = v1.value * og
        v1.backward(dv1)
        v2.backward(dv2)
    }
  }

  object MultiplyFunction {
    def props(v1: Variable, v2: Variable): Props =
      Props(new MultiplyFunction(v1, v2))
  }

  case object Forward
  case class Backward(og: Double)
  case object Result

  case class Var(d: Double, g: Double = 0)

}
