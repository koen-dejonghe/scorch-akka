package scorch.autograd

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import botkop.numsca.Tensor
import botkop.{numsca => ns}

case object Forward
case class Backward(v: Variable)

case class Variable(data: Tensor, gradFn: Option[ActorRef] = None)(
    implicit system: ActorSystem) {
  lazy val grad: Variable = Variable(ns.zerosLike(data))
  def backward(gradOutput: Variable): Unit = {
    grad.data += gradOutput.data
    for (gf <- gradFn) gf ! gradOutput
  }

  def +(d: Double): Variable = {
    val ref: ActorRef = system.actorOf(AddConstant.props(this, d))
    ???
  }
}

case class AddConstant(v: Variable, d: Double) extends Actor {
  val result = Variable(v.data + d, Some(self))
  override def receive: Receive = {
    case Forward =>
      sender ! result
    case gradOutput: Variable =>
      v.backward(gradOutput)
  }
}

object AddConstant {
  def props(v: Variable, d: Double) = Props(new AddConstant(v, d))
}
