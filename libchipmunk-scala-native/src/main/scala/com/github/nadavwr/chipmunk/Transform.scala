package com.github.nadavwr.chipmunk

import com.github.nadavwr.ffi._

import scala.language.implicitConversions
import scala.scalanative.native
import scala.scalanative.native._
import scala.scalanative.runtime.GC

object Transform {
  trait Implicits {
    implicit val ffiTypeOfTransform: FfiType[Transform.Value] =
      FfiType.struct[Transform.Value]("Transform",
        FfiType[CDouble], FfiType[CDouble], FfiType[CDouble],
        FfiType[CDouble], FfiType[CDouble], FfiType[CDouble])

    implicit def transformFromPtr(ptr: Transform.Ptr): Transform = new Transform(ptr)
  }
  object Implicits extends Implicits

  type Value = CStruct6[CDouble, CDouble, CDouble, CDouble, CDouble, CDouble]
  type Ptr = native.Ptr[Value]
  def apply(a: CDouble = 1, b: CDouble = 0,
            c: CDouble = 0, d: CDouble = 1,
            tx: CDouble = 0, ty: CDouble = 0): Transform = {
    val ptr = GC.malloc_atomic(sizeof[Value]).cast[Transform.Ptr]
    val instance = new Transform(ptr)
    instance.a = a
    instance.b = b
    instance.c = c
    instance.d = d
    instance.tx = tx
    instance.ty = ty
    instance
  }
  val identity: Transform = Transform(1, 0,
                                      0, 1,
                                      0, 0)
  def rotate(theta: Double): Transform = {
    val cosTheta = math.cos(theta)
    val sinTheta = math.sin(theta)
    Transform(cosTheta, -sinTheta,
              sinTheta, cosTheta)
  }
  def translate(offset: Vector): Transform =
    Transform(tx = offset.x, ty = offset.y)
}

class Transform(val ptr: Transform.Ptr) extends AnyVal {
  def a: CDouble = !ptr._1
  def b: CDouble = !ptr._2
  def c: CDouble = !ptr._3
  def d: CDouble = !ptr._4
  def tx: CDouble = !ptr._5
  def ty: CDouble = !ptr._6
  def a_=(value: CDouble): Unit = !ptr._1 = value
  def b_=(value: CDouble): Unit = !ptr._2 = value
  def c_=(value: CDouble): Unit = !ptr._3 = value
  def d_=(value: CDouble): Unit = !ptr._4 = value
  def tx_=(value: CDouble): Unit = !ptr._5 = value
  def ty_=(value: CDouble): Unit = !ptr._6 = value
  def :=(other: Transform): Unit = {
    a = other.a
    b = other.b
    c = other.c
    d = other.d
    tx = other.tx
    ty = other.ty
  }

  def compose(other: Transform): Transform = {
    val A = this
    val B = other
    Transform(A.a*B.a + A.b*B.c, A.a*B.b + A.b*B.d,
              A.c*B.a + A.d*B.c, A.c*B.b + A.d*B.d,
              A.tx + B.tx, A.ty + B.ty)
  }

  def apply(v: Vector): Vector =
    Vector(a*v.x + b*v.y + tx, c*v.x + d*v.y + ty)

  def offset: Vector = Vector(tx, ty)
}
