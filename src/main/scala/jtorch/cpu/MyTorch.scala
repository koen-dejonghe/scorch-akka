package jtorch.cpu

import java.util

import com.google.common.base.{FinalizablePhantomReference, FinalizableReferenceQueue, FinalizableSoftReference, FinalizableWeakReference}
import com.google.common.collect.Sets
object MyTorch extends App {

  /*
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
   */

  val t3 = Tensor(100, 100)

  for(i <- 1 to 100) {
    val t = Tensor(300000, 300000)
    Thread.sleep(1)
    println(TH.THFloatTensor_desc(t3.payload).getStr)
  }

  println(Tensor.references.size())
}

case class Tensor private (payload: SWIGTYPE_p_THFloatTensor)

object Tensor {
  type MyRef = FinalizableWeakReference[Long]

  val frq = new FinalizableReferenceQueue()
  val references: util.Set[MyRef] = Sets.newConcurrentHashSet[MyRef]()

  def apply(d1: Long, d2: Long): Tensor = {

    val cPtr = THJNI.THFloatTensor_newWithSize2d(d1, d2)
    val t = new SWIGTYPE_p_THFloatTensor(cPtr, false)

    val tensor = Tensor(t)

    val ref = new MyRef(cPtr, frq) {
      override def finalizeReferent(): Unit = {
        println(s"freeing tensor $cPtr")
        references.remove(this)
        THJNI.THFloatTensor_free(cPtr)
      }
    }

    references.add(ref)

    if (references.size() % 10 == 0) {
      println(references.size())
      System.gc()
    }

    tensor
  }
}
