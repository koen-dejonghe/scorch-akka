package botkop.akka.imperative

object Imperative extends App {

  val x = Variable(3)
  val y = Variable(5)

  val a = x * y
  a.backward(1)

  println(a)
  println(x.g)
  println(y.g)

  case class Variable(d: Double, fn: Option[Function] = None) {
    var g: Double = 0

    def backward(og: Double): Unit = {
      g += og
      fn.foreach(f => f.backward(og))
    }

    def *(that: Variable): Variable = Multiply(this, that).forward()
  }

  trait Function {
    def forward(): Variable
    def backward(og: Double): Unit
  }

  case class Multiply(v1: Variable, v2: Variable) extends Function {
    override def forward(): Variable = {
      Variable(v1.d * v2.d, Some(this))
    }

    override def backward(og: Double): Unit = {
      val dv1 = v2.d * og
      val dv2 = v1.d * og
      v1.backward(dv1)
      v2.backward(dv2)
    }
  }
}


