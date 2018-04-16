package scorch.streams

import akka.stream.javadsl.Sink
import akka.stream.scaladsl.Source
import akka.stream._
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.{ByteString, Timeout}

import scala.concurrent._
import scala.concurrent.duration._

import scorch.autograd.Variable
import scorch.nn.{Linear, Module}
import scorch._
import botkop.{numsca => ns}
import scorch.nn.Infer.Id
import scorch.streams.Layer.{Backward, Forward}

import scala.language.postfixOps

class Layer(module: Module[Id]) extends Actor {

  override def receive: Receive = {
    case next: ActorRef =>
      context become forward(next)
  }

  def forward(next: ActorRef): Receive = {
    case Forward(v) =>
      val activation = module.forward(v)
      next ! Forward(activation)
      context become backward(sender(), activation)
  }

  def backward(prev: ActorRef, v: Variable): Receive = {
    case Backward(g: Variable) =>
      v.backward(g)
      prev ! Backward(v.grad)
      context become forward(sender())
  }
}
object Layer {
  case class Forward(v: Variable)
  case class Backward(grad: Variable)
  def props(m: Module[Id]) = Props(new Layer(m))
}

class LoopbackActor(next: ActorRef) extends Actor {

  // forward pass
  override def receive: Receive = {
    case (x: Variable, y: Variable) =>
      next ! Forward(x)
      context become iteration(sender(), x, y)
  }

  // backward pass
  def iteration(requester: ActorRef, x: Variable, y: Variable): Receive = {
    case Forward(yHat) =>
      val loss = softmaxLoss(yHat, y)
      loss.backward()
      requester ! loss

      // sender() ! Backward(loss.grad)

      context become receive
  }
}
object LoopbackActor {
  def props(next: ActorRef) = Props(new LoopbackActor(next))
}

object ModuleStream extends App {

  implicit val system: ActorSystem = ActorSystem("QuickStart")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val numSamples = 128
  val numClasses = 10
  val nf1 = 40
  val nf2 = 20

  val fc1 = Linear(nf1, nf2)
  val fc2 = Linear(nf2, numClasses)

  val afc1 = system.actorOf(Layer.props(fc1))
  val afc2 = system.actorOf(Layer.props(fc2))
  val lp = system.actorOf(LoopbackActor.props(afc1))

  // link/chain the actor network
  // make afc2 next of afc1
  afc1 ! afc2
  // make lp next of afc2
  afc2 ! lp

  val input = Variable(ns.randn(numSamples, nf1))
  val target = Variable(ns.randint(numClasses, Array(numSamples, 1)))

  import akka.pattern.ask
  implicit val timeout: Timeout = Timeout(5 seconds)

  (lp ? (input, target)).map{v =>
    println(v)
    println(input.grad)
  }

  /*
  val source: Source[Int, NotUsed] = Source(1 to 100)
  val done: Future[Done] = source.runForeach(i ⇒ println(i))
  done.onComplete(_ ⇒ system.terminate())
  */

}


