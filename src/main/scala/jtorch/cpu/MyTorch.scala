package jtorch.cpu

import java.util
import java.util.concurrent.atomic.AtomicInteger

import com.google.common.base.{
  FinalizablePhantomReference,
  FinalizableReferenceQueue,
  FinalizableSoftReference,
  FinalizableWeakReference
}
import com.google.common.collect.Sets
object MyTorch extends App {

  val t3 = Tensor(100, 100)

  for (i <- 1 to 100) {
    val t = Tensor(300000, 300000)
    Thread.sleep(1)
  }

  println("***************** DONE")
  println(t3.cPtr)
  println(t3.payload)
  println(TH.THFloatTensor_desc(t3.payload).getStr)
  // println(Tensor.references.size())
}

case class Tensor private (payload: SWIGTYPE_p_THFloatTensor, cPtr: Long) {
  // private val d = Array.fill(1000000)(0.0)

  override def finalize(): Unit = {
    println(s"freeing $cPtr")
    THJNI.THFloatTensor_free(cPtr)
  }

}

object Tensor {

  val count = new AtomicInteger(0)

  /*
  type MyRef = FinalizableWeakReference[Long]

  val frq = new FinalizableReferenceQueue()
  val references: util.Set[MyRef] = Sets.newConcurrentHashSet[MyRef]()


  def makeRef(ptr: Long): MyRef {
    def finalizeReferent(): Unit
  } = new MyRef(ptr, frq) {
    override def finalizeReferent(): Unit = {
      println(s"freeing tensor $ptr")
      references.remove(this)
      THJNI.THFloatTensor_free(ptr)
    }
  }
  */

  def apply(d1: Long, d2: Long): Tensor = {

    val cPtr = THJNI.THFloatTensor_newWithSize2d(d1, d2)
    val t = new SWIGTYPE_p_THFloatTensor(cPtr, false)
    val tensor = Tensor(t, cPtr)

    println(s"creating tensor $cPtr")

    /*
    val ref = makeRef(cPtr)
    references.add(ref)

    if (references.size() % 10 == 0) {
      println(references.size())
      System.gc()
    }
    */

    if (count.incrementAndGet() % 10 == 0) {
      println(count)
      System.gc()
    }

    tensor

  }
}
