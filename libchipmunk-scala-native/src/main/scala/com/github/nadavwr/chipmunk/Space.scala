package com.github.nadavwr.chipmunk

import scala.collection.mutable
import scala.language.implicitConversions
import scala.scalanative.native
import scala.scalanative.native._

object Space {
  type Value = CStruct0
  type Ptr = native.Ptr[Value]

  trait Implicits {
    implicit def spaceFromPtr(ptr: Space.Ptr): Space =
      if (ptr == null) null else allSpaces(ptr.cast[Long])
  }
  object Implicits extends Implicits

  def apply(): Space = {
    val spacePtr = impl.cpSpaceNew()
    val space = new Space(spacePtr)
    allSpaces += space.id -> space
    space
  }

  val allSpaces: mutable.Map[Long, Space] = mutable.HashMap.empty
}

class Space(val ptr: Space.Ptr) {
  private def spacePtr: Space.Ptr = ptr
  def id: Long = spacePtr.cast[Long]
  def dispose(): Unit = {
    for (body <- bodies) {
      removeBody(body)
      body.dispose()
    }
    CollisionHandler.disposeFor(id)
    impl.cpSpaceFree(spacePtr)
    Space.allSpaces -= id
  }
  def iterations: Int = impl.cpSpaceGetIterations(spacePtr)
  def iterations_=(value: Int): Unit = impl.cpSpaceSetIterations(spacePtr, value)
  def dampingFactor: Double = impl.cpSpaceGetDamping(spacePtr)
  def dampingFactor_=(value: Double): Unit = impl.cpSpaceSetDamping(spacePtr, value)
  def timestep: Double = impl.cpSpaceGetCurrentTimeStep(spacePtr)
  def addBody(body: Body): Unit = {
    impl.cpSpaceAddBody(spacePtr, body.ptr)
    body.shapes.foreach(addShape)
    bodies += body
  }
  def removeBody(body: Body): Unit = {
    body.shapes.foreach(removeShape)
    bodies -= body
    impl.cpSpaceRemoveBody(spacePtr, body.ptr)
  }
  def addShape(shape: Shape): Unit = {
    impl.cpSpaceAddShape(spacePtr, shape.ptr)
    shapes += shape
  }
  def removeShape(shape: Shape): Unit = {
    shapes -= shape
    impl.cpSpaceRemoveShape(spacePtr, shape.ptr)
  }

  def finegrainedCollisionHandling() {
    onBegin { arbiter =>
      val (a, b) = arbiter.bodies
      Seq(a, b).forall { body =>
        body.onBeginOpt.fold(true) { onBegin => onBegin(arbiter) }
      }
    }

    onPreSolve { arbiter =>
      val (a, b) = arbiter.bodies
      Seq(a, b).forall { body =>
        body.onPreSolveOpt.fold(true) { onPreSolve => onPreSolve(arbiter) }
      }
    }

    onPostSolve { arbiter =>
      val (a, b) = arbiter.bodies
      Seq(a, b).foreach { body =>
        body.onPostSolveOpt.foreach(onPostSolve => onPostSolve(arbiter))
      }
    }

    onSeparate { arbiter =>
      val (a, b) = arbiter.bodies
      Seq(a, b).foreach { body =>
        body.onSeparateOpt.foreach(onSeparate => onSeparate(arbiter))
      }
    }
  }

  def onBegin(handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Default)
      .beginHandlerOpt = Some(handler)
  }

  def onBegin(collisionType: ULong, handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Wildcard(collisionType))
      .beginHandlerOpt = Some(handler)
  }

  def onBegin(aType: ULong, bType: ULong, handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Specific(aType, bType))
      .beginHandlerOpt = Some(handler)
  }

  def onPreSolve(handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Default)
      .preSolveHandlerOpt = Some(handler)
  }

  def onPreSolve(collisionType: ULong, handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Wildcard(collisionType))
      .preSolveHandlerOpt = Some(handler)
  }

  def onPreSolve(aType: ULong, bType: ULong, handler: Arbiter => Boolean): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Specific(aType, bType))
      .preSolveHandlerOpt = Some(handler)
  }

  def onPostSolve(handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Default)
      .postSolveHandlerOpt = Some(handler)
  }

  def onPostSolve(collisionType: ULong, handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Wildcard(collisionType))
      .postSolveHandlerOpt = Some(handler)
  }

  def onPostSolve(aType: ULong, bType: ULong, handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Specific(aType, bType))
      .postSolveHandlerOpt = Some(handler)
  }

  def onSeparate(handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Default)
      .separateHandlerOpt = Some(handler)
  }

  def onSeparate(collisionType: ULong, handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Wildcard(collisionType))
      .separateHandlerOpt = Some(handler)
  }

  def onSeparate(aType: ULong, bType: ULong, handler: Arbiter => Unit): Unit = {
    CollisionHandler.handlerFor(this, CollisionHandler.CollisionSpec.Specific(aType, bType))
      .separateHandlerOpt = Some(handler)
  }

  def step(dt: Double): Unit = impl.cpSpaceStep(spacePtr, dt)

  val bodies: mutable.Set[Body] = mutable.HashSet.empty
  val shapes: mutable.Set[Shape] = mutable.HashSet.empty
}
