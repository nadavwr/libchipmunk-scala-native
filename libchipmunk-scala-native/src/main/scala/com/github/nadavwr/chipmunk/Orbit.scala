package com.github.nadavwr.chipmunk
import scala.math._

object Orbit {
  val G = 6.6740831e-11
  sealed trait Type
  case object Elliptic extends Type
  case object Parabolic extends Type
  case object Hyperbolic extends Type
}
class Orbit(val parent: Body, val satellite: Body) {
  import Orbit._

  class State {
    val M: Double = parent.mass
    val m: Double = satellite.mass
    val μ: Double = (M + m) * G// MmG
    val p: Vector = satellite.position - parent.position
    val r: Double = p.r
    val x: Double = p.x
    val y: Double = p.y
    val v: Vector = satellite.velocity - parent.velocity
    val h: Double = p⨯v // angular momentum
    val (ex, ey, e) = {
      val eccentricity = Vector(v.y*h/μ - x/r, -v.x*h/μ - y/r)
      (eccentricity.x, eccentricity.y, eccentricity.r)
    }
    def isElliptic: Boolean = e < 1
    def isParabolic: Boolean = e == 1
    def isHyperbolic: Boolean = e > 1
    def orbitType: Orbit.Type = {
      if (isElliptic) Orbit.Elliptic
      else if (isParabolic) Orbit.Parabolic
      else Orbit.Hyperbolic
    }
    val a: Double = pow(h, 2)/(μ*(1 - pow(e, 2))) //semi-major axis
    val E: Double = orbitType match {
      case Elliptic => -μ/(2*a)
      case Parabolic => 0
      case Hyperbolic => μ/(2*a)
    }
    val T: Double = 2*Pi*sqrt(pow(a, 3)/μ) // orbital period
    val ω: Double = atan(ey/ex)
    def d(θ: Double): Double = a*(1 - pow(e, 2)) / (1 + e*cos(θ+ω))


  }
}
