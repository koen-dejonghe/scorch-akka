package nn.akka

import botkop.numsca.Tensor

class Net {

}

abstract class Layer(parameters: Seq[Tensor], next: Layer, prev: Layer) {
  def forward(x: Tensor): Unit
  def backward(g: Tensor): Unit
}


