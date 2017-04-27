package com.github.nadavwr.chipmunk

import com.github.nadavwr.ffi._

import scala.language.implicitConversions
import scala.math.atan2
import scala.scalanative.native
import scala.scalanative.native._
import scala.scalanative.runtime.GC

object Vector {
  type Value = CStruct2[CDouble, CDouble]
  type Ptr = native.Ptr[Value]

  trait Implicits {
    implicit val ffiTypeOfVector: FfiType[Vector.Value] =
      FfiType.struct[Vector.Value]("Vector", FfiType[CDouble], FfiType[CDouble])

    implicit def vectorFromPtr(ptr: Vector.Ptr): Vector =
      new Vector(ptr)
  }
  object Implicits extends Implicits

  def zero = Vector(0, 0)

  def apply(x: Double, y: Double): Vector = {
    val vector = GC.malloc(sizeof[Value]).cast[Ptr]
    vector.x = x
    vector.y = y
    vector
  }
  def polar(r: Double, theta: Double): Vector = {
    apply(r*math.cos(theta), r*math.sin(theta))
  }
  def unapply(vector: Vector): Option[(Double, Double)] = {
    if (vector.ptr == null) None
    else Some(vector.x, vector.y)
  }
}

class Vector(val ptr: Vector.Ptr) extends AnyVal {
  def x: CDouble = !ptr._1
  def y: CDouble = !ptr._2
  def x_=(x: CDouble): Unit = !ptr._1 = x
  def y_=(y: CDouble): Unit = !ptr._2 = y
  def :=(other: Vector): Unit = {
    x = other.x
    y = other.y
  }

  override def toString: String = {
    s"Vector($x, $y)"
  }

  def +(other: Vector): Vector = {
    Vector(x + other.x, y + other.y)
  }

  def -(other: Vector): Vector = {
    Vector(x - other.x, y - other.y)
  }

  def unary_-(): Vector = this * -1

  def *(scalar: Double): Vector = {
    Vector(x*scalar, y*scalar)
  }

  def /(scalar: Double): Vector = {
    Vector(x/scalar, y/scalar)
  }

  def r: Double = math.sqrt(x*x + y*y)

  def normal: Vector = this / r

  def theta: Double =
    (atan2(y, x) + 2 * scala.math.Pi) % (2 * scala.math.Pi)

  def cross(other: Vector): Double = x*other.y - y*other.x
  def ⨯(other: Vector): Double = cross(other)

  def dot(other: Vector): Double = x*other.x + y*other.y
  def ⋅(other: Vector): Double = dot(other)
}
