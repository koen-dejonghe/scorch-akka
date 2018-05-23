package nn.jtorch

import jtorch.cpu._
import jtorch.cpu.TH._
object MyTorch extends App {

  println(System.getProperty("java.library.path"))
  val x = THJNI.new_floatArray(3)
  println(x)

  val t: SWIGTYPE_p_THFloatTensor = TH.THFloatTensor_new()
  TH.THFloatTensor_arange(t, 0, 10, 1)
  println(t)

  val desc = THFloatTensor_desc(t)
  println(desc.getStr)
  desc.delete()

  val f = TH.THFloatTensor_get1d(t, 4)
  println(f)


  val s2 = TH.THLongStorage_newWithSize2(3, 3)
  val t2 = TH.THFloatTensor_newWithSize2d(3, 3)
  // THFloatTensor_arange(t2, 0, 12, 1)
  println(THFloatTensor_desc(t2).getStr)

}
