package nn.akka

import botkop.numsca.Tensor

class Net {

}

abstract class Layer(parameters: Seq[Tensor], next: Layer, prev: Layer) {
  def forward(x: Tensor): Unit
  def backward(g: Tensor): Unit
}

case class Linear(parameters: Seq[Tensor]) extends Layer(parameters) {

  val Seq(w, b) = parameters

  override def forward(next: Layer, x: Tensor): Unit = {
    val yHat = w dot x + b
    next.f
  }

  override def backward(pre: Layer, g: Tensor): Unit = ???
}
