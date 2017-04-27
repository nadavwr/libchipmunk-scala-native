package com.github.nadavwr.chipmunk

import com.github.nadavwr.ffi._

import scala.language.implicitConversions
import scala.scalanative.native
import scala.scalanative.native._
import scala.scalanative.runtime.GC

object BoundingBox {
  trait Implicits {
    implicit val ffiTypeOfBoundingBox: FfiType[BoundingBox.Value] =
      FfiType.struct[BoundingBox.Value]("BoundingBox",
        FfiType[CDouble], FfiType[CDouble], FfiType[CDouble], FfiType[CDouble])
    implicit def boundingBoxFromPtr(ptr: BoundingBox.Ptr): BoundingBox = new BoundingBox(ptr)
  }
  object Implicits extends Implicits

  type Value = CStruct4[CDouble, CDouble, CDouble, CDouble]
  type Ptr = native.Ptr[Value]
  def apply(l: CDouble, b: CDouble, r: CDouble, t: CDouble): BoundingBox = {
    val ptr = GC.malloc_atomic(sizeof[Value]).cast[Ptr]
    val instance = new BoundingBox(ptr)
    instance.l = l
    instance.b = b
    instance.r = r
    instance.t = t
    instance
  }
}
class BoundingBox(val ptr: BoundingBox.Ptr) extends AnyVal {
  def l: CDouble = !ptr._1
  def b: CDouble = !ptr._2
  def r: CDouble = !ptr._3
  def t: CDouble = !ptr._4
  def l_=(value: CDouble): Unit = !ptr._1 = value
  def b_=(value: CDouble): Unit = !ptr._2 = value
  def r_=(value: CDouble): Unit = !ptr._3 = value
  def t_=(value: CDouble): Unit = !ptr._4 = value
  def :=(other: BoundingBox): Unit = {
    l = other.l
    b = other.b
    r = other.r
    t = other.t
  }
}
