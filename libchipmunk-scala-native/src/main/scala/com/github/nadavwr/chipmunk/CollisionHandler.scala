package com.github.nadavwr.chipmunk

import scala.collection.mutable
import scala.language.implicitConversions
import scala.scalanative.native._

object CollisionHandler {
  type Value = impl.cpCollisionHandler

  trait Implicits {
    implicit def handlerFromPtr(ptr: Ptr[CollisionHandler.Value]): CollisionHandler =
      if (ptr == null) null else allHandlers(ptr.cast[Long])
  }
  object Implicits extends Implicits


  sealed trait CollisionSpec {
    def toPtr: Ptr[CollisionSpec] = this.asInstanceOf[_Object].cast[Ptr[CollisionSpec]]
    def isWildcard: Boolean = this.isInstanceOf[CollisionSpec.Wildcard]
  }
  object CollisionSpec {
    def fromPtr(specPtr: Ptr[CollisionSpec]): CollisionSpec = specPtr.cast[_Object].asInstanceOf[CollisionSpec]
    case object Default extends CollisionSpec
    case class Wildcard(collisionType: ULong) extends CollisionSpec
    case class Specific(a: ULong, b: ULong) extends CollisionSpec
  }

  val allHandlers: mutable.Map[Long, CollisionHandler] = mutable.HashMap.empty
  val bySpec: mutable.Map[(Long, CollisionSpec), CollisionHandler] = mutable.HashMap.empty

  def _begin(arbiterPtr: Ptr[Arbiter.Value], spacePtr: Space.Ptr, data: Ptr[Byte]): CInt = {
    val arbiter = new Arbiter(arbiterPtr, spacePtr)
    val handlerId = data.cast[Long]
    val result = allHandlers(handlerId).beginHandler(arbiter)
    if (result) 1 else 0
  }

  def _preSolve(arbiterPtr: Ptr[Arbiter.Value], spacePtr: Space.Ptr, data: Ptr[Byte]): CInt = {
    val arbiter = new Arbiter(arbiterPtr, spacePtr)
    val handlerId = data.cast[Long]
    val result = allHandlers(handlerId).preSolveHandler(arbiter)
    if (result) 1 else 0
  }

  def _postSolve(arbiterPtr: Ptr[Arbiter.Value], spacePtr: Space.Ptr, data: Ptr[Byte]): Unit = {
    val arbiter = new Arbiter(arbiterPtr, spacePtr)
    val handlerId = data.cast[Long]
    allHandlers(handlerId).postSolveHandler(arbiter)
  }

  def _separate(arbiterPtr: Ptr[Arbiter.Value], spacePtr: Space.Ptr, data: Ptr[Byte]): Unit = {
    val arbiter = new Arbiter(arbiterPtr, spacePtr)
    val handlerId = data.cast[Long]
    allHandlers(handlerId).separateHandler(arbiter)
  }

  private def create(space: Space, spec: CollisionSpec): CollisionHandler = {
    val handlerPtr = spec match {
      case CollisionSpec.Default =>
        impl.cpSpaceAddDefaultCollisionHandler(space.ptr)
      case CollisionSpec.Wildcard(collisionType) =>
        impl.cpSpaceAddWildcardHandler(space.ptr, collisionType)
      case CollisionSpec.Specific(a, b) =>
        impl.cpSpaceAddCollisionHandler(space.ptr, a, b)
    }
    val handlerId = handlerPtr.cast[Long]
    !handlerPtr._3 = _begin _
    !handlerPtr._4 = _preSolve _
    !handlerPtr._5 = _postSolve _
    !handlerPtr._6 = _separate _
    !handlerPtr._7.cast[Ptr[Long]] = handlerId
    val handler = CollisionHandler(handlerId, spec)
    allHandlers += handlerId -> handler
    handler
  }

  def handlerFor(space: Space, spec: CollisionSpec): CollisionHandler =
    bySpec.getOrElseUpdate((space.id, spec), create(space, spec))

  def disposeFor(spaceId: Long): Unit = {
    val handlerIds = bySpec.collect {
      case (key@(`spaceId`, _), handler) =>
        bySpec -= key
        handler.id
    }
    allHandlers --= handlerIds
  }

}

case class CollisionHandler(
    id: Long,
    spec: CollisionHandler.CollisionSpec,
    var beginHandlerOpt: Option[Arbiter => Boolean] = None,
    var preSolveHandlerOpt: Option[Arbiter => Boolean] = None,
    var postSolveHandlerOpt: Option[Arbiter => Unit] = None,
    var separateHandlerOpt: Option[Arbiter => Unit] = None) {
  def beginHandler(arbiter: Arbiter): Boolean = {
    val handler = beginHandlerOpt.getOrElse {
      (arbiter: Arbiter) =>
        spec.isWildcard || (arbiter.a.begin() && arbiter.b.begin())
    }
    handler(arbiter)
  }
  def preSolveHandler(arbiter: Arbiter): Boolean = {
    val handler = preSolveHandlerOpt.getOrElse {
      (arbiter: Arbiter) =>
        spec.isWildcard || (arbiter.a.preSolve() && arbiter.b.preSolve())
    }
    handler(arbiter)
  }
  def postSolveHandler(arbiter: Arbiter): Unit = {
    val handler = postSolveHandlerOpt.getOrElse {
      (arbiter: Arbiter) =>
        if (!spec.isWildcard) {
          arbiter.a.postSolve()
          arbiter.b.postSolve()
        }
    }
    handler(arbiter)
  }
  def separateHandler(arbiter: Arbiter): Unit = {
    val handler = separateHandlerOpt.getOrElse {
      (arbiter: Arbiter) =>
        if (!spec.isWildcard) {
          arbiter.a.separate()
          arbiter.b.separate()
        }
    }
    handler(arbiter)
  }
}
