package scorch.autograd

import akka.actor.{Actor, ActorRef, Props}
import botkop.numsca.Tensor
import botkop.{numsca => ns}

case object Forward
case class Backward(t: Tensor)

case class Variable(data: Tensor, gradFn: Option[ActorRef] = None) extends Actor {
  lazy val grad: Tensor = ns.zerosLike(data)
  override def receive: Receive = {
    case Backward(gradOutput: Tensor) =>
      grad += gradOutput
      for (gf <- gradFn) gf ! Backward(gradOutput)
  }
}

object Variable {
  def props(data: Tensor, gradFn: Option[ActorRef] = None): Props =
    Props(new Variable(data, gradFn))
}

case class AddConstant(v: Variable, d: Double) extends Actor {
  override def receive: Receive = {
    case Forward =>
      context.actorOf(Variable.props(data = v.data + d, gradFn = Some(self)))
    case Backward(gradOutput: Tensor) =>
      v ! Backward
  }
}
