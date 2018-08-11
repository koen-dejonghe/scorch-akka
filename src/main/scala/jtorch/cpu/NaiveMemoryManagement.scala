package jtorch.cpu

import java.util.concurrent.atomic.AtomicLong

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object NaiveMemoryManagement extends App with LazyLogging {

  logger.info("*** starting sequential **************************")
  sequential()
//  logger.info("*** starting parallel ****************************")
//  parallel()

  def sequential(): Unit = {
    val t3 = MyTensor.zeros(100, 100) // this one will only get garbage collected at the end of the program

    for (i <- 1 to 100) {
      MyTensor.zeros(3000, 3000) // these will get GC'ed as soon as as System.gc() is called
      println(i)
      Thread.sleep(1000)
    }

    logger.info("DONE")
    logger.info(t3.cPtr.toString)
    logger.info(t3.payload.toString)
    logger.info(TH.THFloatTensor_desc(t3.payload).getStr) // this should still work
    logger.info(TH.THFloatTensor_get2d(t3.payload, 10, 10).toString)

    val t4 = t3 + 3f

    logger.info(TH.THFloatTensor_get2d(t4.payload, 10, 10).toString)
  }

  def parallel(): Unit = {

    import scala.concurrent.ExecutionContext.Implicits.global

    val t3 = MyTensor.zeros(100, 100) // this one will only get garbage collected at the end of the program

    val futures = Future.sequence {
      (1 to 100).map { _ =>
        Future {
          val tt = MyTensor.ones(3000, 3000) + Random.nextFloat()
          logger.info(tt(10, 10).toString)
          Thread.sleep(1)
        }
      }
    }

    Await.result(futures, 10 seconds)

    logger.info("DONE")
    logger.info(t3.cPtr.toString)
    logger.info(t3.payload.toString)
    logger.info(TH.THFloatTensor_desc(t3.payload).getStr) // this should still work
    logger.info(t3(10, 10).toString)
  }

}

case class MyTensor private (payload: SWIGTYPE_p_THFloatTensor,
                             cPtr: Long,
                             size: Long)
    extends LazyLogging {

  override def finalize(): Unit = {
    THJNI.THFloatTensor_free(cPtr)
    val memSize = MyTensor.hiMemMark.addAndGet(-size)
    logger.info(s"freeing $cPtr (mem = $memSize)")
  }

  def apply(i1: Long, i2: Long): Float =
    TH.THFloatTensor_get2d(this.payload, i1, i2)

  def +(f: Float): MyTensor = {
    val result = MyTensor.makeTensorLike(this)
    TH.THFloatTensor_add(result.payload, this.payload, f)
    result
  }

}

object MyTensor extends LazyLogging {

  val threshold: Long = 2L * 1024L * 1024L * 1024L // 2 GB

  val hiMemMark = new AtomicLong(0)

  def memCheck(size: Long): Unit =
    if (hiMemMark.addAndGet(size) > threshold) {
      logger.info("not calling gc")
      System.gc()
    }

  def zeros(d1: Long, d2: Long): MyTensor = {
    val t = makeTensor(d1, d2)
    val storage = TH.THLongStorage_newWithSize2(d1, d2)
    TH.THFloatTensor_zeros(t.payload, storage)
    t
  }

  def ones(d1: Long, d2: Long): MyTensor = {
    val t = makeTensor(d1, d2)
    val storage = TH.THLongStorage_newWithSize2(d1, d2)
    TH.THFloatTensor_ones(t.payload, storage)
    t
  }

  // boiler plate to create a Torch tensor of floats
  def makeTensor(d1: Long, d2: Long): MyTensor = {
    //val cPtr = THJNI.THFloatTensor_newWithSize2d(d1, d2)
    val cPtr = THJNI.THFloatTensor_new()

    val t = new SWIGTYPE_p_THFloatTensor(cPtr, false)
    val size = d1 * d2 * 4
    memCheck(size)
    MyTensor(t, cPtr, size)
  }

  def makeTensorLike(other: MyTensor): MyTensor = {
    memCheck(other.size)
    val cPtr = THJNI.THFloatTensor_newWithTensor(other.cPtr)
    val t = new SWIGTYPE_p_THFloatTensor(cPtr, false)
    MyTensor(t, cPtr, other.size)
  }
}
