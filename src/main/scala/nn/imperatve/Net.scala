package nn.imperatve

import botkop.{numsca => ns}
import botkop.numsca.Tensor

object Net extends App {


  val a = Variable(ns.arange(10.0))
  val b = Variable(ns.ones(1, 10))

  val c = a + b
  val g = Variable(ns.randn(1, 10))
  c.backward(g)

  println(a.grad)
  println(b.grad)
  println(c.grad)

}

case class Variable(data: Tensor, gf: Option[Function] = None) {

  lazy val grad: Variable = Variable(ns.zerosLike(data))

  def backward(): Unit = backward(Variable(ns.ones(data.shape)))

  def backward(g: Variable): Unit = {
    grad.data += g.data
    gf foreach(f => f backward g)
  }

  def +(that: Variable): Variable = AddFunction(this, that).forward()
}

trait Function {
  def forward(): Variable
  def backward(g: Variable): Unit
}

case class AddFunction(a: Variable, b: Variable) extends Function {
  override def forward(): Variable = {
    Variable(a.data + b.data, Some(this))
  }

  override def backward(g: Variable): Unit = {
    a.backward(g)
    b.backward(g)
  }
}
