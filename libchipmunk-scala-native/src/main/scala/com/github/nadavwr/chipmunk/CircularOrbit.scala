package com.github.nadavwr.chipmunk
import scala.math._

case class CircularOrbit(parent: Body, satellite: Body) { orbit =>
  require(parent.space == satellite.space)
  val space: Space = parent.space
  val GM: Double = {
    def G = 6.6740831e-11
    def M = parent.mass + satellite.mass
    M*G
  }

  class State {
    val r: Double = current.r
    val T: Double = 2*Pi*sqrt(pow(r, 3) / GM)
    val ω: Double = 2*Pi/T
    val speed: Double = ω*r
  }

  var state: State = new State
  def reset(): Unit = { state = new State }

  def r: Double = state.r
  def T: Double = state.T
  def ω: Double = state.ω
  def speed: Double = state.speed
  private var _enabled: Boolean = true
  def enabled: Boolean = _enabled
  def enable(): Unit = {
    if (!enabled) {
      _enabled = true
      reset()
    }
  }
  def disable(): Unit = {
    if (enabled) _enabled = false
  }

  object current {
    def p: Vector = satellite.position - parent.position
    def r: Double = p.r
    def θ: Double = (satellite.position - parent.position).theta
    def t: Double = T * θ / (2*Pi)
    def v: Vector = orbit.v(t)
    def drift: Double = r - orbit.r
  }

  def θ(t: Double): Double = 2*Pi * t / T
  def p(t: Double): Vector = parent.position + Vector.polar(r, θ(t))
  def v(t: Double): Vector = parent.velocity + Vector.polar(speed, θ(t) + Pi/2)
  val velocityHandler: Body.VelocityHandler = (body: Body, gravity: Vector, dt: Double) => {
    body.defaultVelocityHandler(body, gravity, dt)
    if (orbit.enabled) {
      satellite.velocity = (p(current.t + dt) - current.p) / dt
    }
  }
  val positionHandler: Body.PositionHandler = (body: Body, dt: Double) => {
    if (orbit.enabled) {
      val p1 = p(current.t + dt)
      body.defaultPositionHandler(satellite, dt)
      satellite.position = p1
    } else {
      body.defaultPositionHandler(satellite, dt)
    }
  }
  def install(): Unit = {
    satellite.velocityHandler(Some(velocityHandler))
    satellite.onBegin { arbiter =>
      if (orbit.enabled) disable()
      true
    }

  }
}
