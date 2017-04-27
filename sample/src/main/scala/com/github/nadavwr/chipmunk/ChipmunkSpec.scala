package com.github.nadavwr.chipmunk

import com.github.nadavwr.makeshift._

import scala.language.implicitConversions
import scala.math._

//noinspection TypeAnnotation
object ChipmunkSpec extends App with Spec {

  trait SpaceFixture extends Fixture {
    lazy val space = Space().withCleanup("space") { _.dispose() }
  }

  trait PlanetFixture extends SpaceFixture {
    val planet =
      Body.define(
        CircleDef(10).withDensity(1)
      ).addAt(space, Vector(0, 0))
  }

  test("body mass & moment calculated from shapes") runWith new PlanetFixture {
    val planetShape = planet.shapes.head.asInstanceOf[CircleShape]
    val expectedMass = planetShape.density * pow(planetShape.radius, 2) * Pi

    assertThat(abs(planet.mass - expectedMass) < 1e-4,
      s"planet mass (${planet.mass}) ?= expected ($expectedMass)")
    assertThat(planet.mass == planetShape.mass,
      s"planet mass (${planet.mass}) ?= shape mass (${planetShape.mass})")
    assertThat(planet.moment == planetShape.moment,
      s"planet moment (${planet.moment}) ?= shape moment (${planetShape.moment})")
  }

  test("register collision with planet") runWith new PlanetFixture {

    val crasher = Body.define(CircleDef(10).withMass(1)).addAt(space, Vector(100, 0))
    println(s"crasher mass: ${crasher.mass}; moment: ${crasher.moment}")

    var collision: Boolean = false
    val collisionHandler = CollisionHandler.handlerFor(space, CollisionHandler.CollisionSpec.Default)
    collisionHandler.beginHandlerOpt = Some(arbiter => { println("collision"); collision = true; true })

    assertThat(space.bodies.contains(crasher), "crasher body ?∈ space")
    assertThat(space.bodies.contains(planet), "planet body ?∈ space")

    val impulse = Vector(-1, 0)
    crasher.applyImpulse(impulse)
    println(s"Crasher \t{ p: ${crasher.position}; \tv: ${crasher.velocity} }")
    println(s"Planet  \t{ p: ${planet.position}; \tv: ${planet.velocity} }")
    println("stepping 120")
    for (i <- 1 to 120) space.step(1)
    println(s"Crasher \t{ p: ${crasher.position}; \tv: ${crasher.velocity} }")
    println(s"Planet  \t{ p: ${planet.position}; \tv: ${planet.velocity} }")
    assertThat(collision, "crasher collided(?) with planet")
  }

  trait EarthFixture extends SpaceFixture {
    val earthMass = 5.972e24 //kg
    val earthRadius = 6.371e6 //m
    val earth = Body.define(CircleDef(earthRadius).withMass(earthMass)).addAt(space, Vector(0, 0))
  }

  test("body mass from shape") runWith new EarthFixture {
    assertThat(earth.mass == earthMass, s"body mass ${earth.mass} ?= shape mass $earthMass")
  }

  trait OrbitFixture extends EarthFixture {
    val orbitRadius = 4.2164172334e7 // geostationary
    val satellite = Body.define(CircleDef(10).withMass(1)).addAt(space, Vector(orbitRadius, 0))
    val orbit = CircularOrbit(earth, satellite)

    def initOrbit(): Unit
    initOrbit()

    val sample = for (i <- 1 to 24) yield {
      for (j <- 1 to 60) space.step(60)
      //if (day == 1) println(s"${orbit.current.h}∠${orbit.current.θ}")
      abs(orbit.current.drift)
    }
    val maxDrift = sample.max
    val maxErrorRate = maxDrift/orbit.r
    def tolerance: Double
    assertThat(maxErrorRate < tolerance,
      s"max orbit error rate ($maxErrorRate) ?< tolerance ($tolerance)")
  }

  test("gravity-based orbit") runWith new OrbitFixture {
    override lazy val tolerance = 1e-2
    def initOrbit(): Unit = {
      satellite.applyImpulse(orbit.current.v)
      satellite.velocityHandler(Some(
        (body: Body, gravity: Vector, dt: Double) => {
          val gravity = {
            val magnitude = orbit.GM / pow(orbit.r, 2)
            val normal = (orbit.parent.position - orbit.satellite.position).normal
            normal * magnitude
          }
          // no gravity param!!!!
          body.defaultVelocityHandler(body.ptr, gravity, dt)
        }))
    }
  }

  test("velocity-based orbit") runWith new OrbitFixture {
    override lazy val tolerance = 1e-5
    def initOrbit(): Unit = {
      satellite.velocityHandler(Some(orbit.velocityHandler))
    }
  }

  test("position-based orbit") runWith new OrbitFixture {
    override lazy val tolerance = 1e-5
    def initOrbit(): Unit = {
      satellite.positionHandler(Some(orbit.positionHandler))
    }
  }

  test("post step callback") runWith new PlanetFixture {
    planet.applyImpulse(Vector(10, 0))
    var counter = 0
    val f = (body: Body) => counter += 1
    planet.addPostStep(f)
    space.step(1)
    space.step(1)
    assertThat(counter == 1,
      s"post-step callback invocation count ($counter) ?= 1")
  }

  test("fine-grained collision handling") runWith new PlanetFixture {
    space.finegrainedCollisionHandling()

  }

}
