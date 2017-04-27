package com.github.nadavwr.chipmunk

import scalanative.native
import scalanative.native._

object Arbiter {
  type Value = CStruct0
}

class Arbiter private[chipmunk] (val arbiterPtr: Ptr[Arbiter.Value], val space: Space) {
  def isFirstContact: Boolean = impl.cpArbiterIsFirstContact(arbiterPtr) != 0
  def isRemoval: Boolean = impl.cpArbiterIsRemoval(arbiterPtr) != 0
  def shapes: (Shape, Shape) = {
    val a, b = stackalloc[Shape.Ptr]
    impl.cpArbiterGetShapes(arbiterPtr, a, b)
    (!a, !b)
  }
  def bodies: (Body, Body) = {
    val a, b = stackalloc[Body.Ptr]
    impl.cpArbiterGetBodies(arbiterPtr, a, b)
    (!a, !b)
  }
  object a {
    def shape: Shape = shapes._1
    def body: Body = bodies._1
    def begin(): Boolean = impl.cpArbiterCallWildcardBeginA(arbiterPtr, space.ptr) != 0
    def preSolve(): Boolean = impl.cpArbiterCallWildcardPreSolveA(arbiterPtr, space.ptr) != 0
    def postSolve(): Unit = impl.cpArbiterCallWildcardPostSolveA(arbiterPtr, space.ptr)
    def separate(): Unit = impl.cpArbiterCallWildcardSeparateA(arbiterPtr, space.ptr)
  }
  object b {
    def shape: Shape = shapes._2
    def body: Body = bodies._2
    def begin(): Boolean = impl.cpArbiterCallWildcardBeginB(arbiterPtr, space.ptr) != 0
    def preSolve(): Boolean = impl.cpArbiterCallWildcardPreSolveB(arbiterPtr, space.ptr) != 0
    def postSolve(): Unit = impl.cpArbiterCallWildcardPostSolveB(arbiterPtr, space.ptr)
    def separate(): Unit = impl.cpArbiterCallWildcardSeparateB(arbiterPtr, space.ptr)
  }
  def totalImpulse: Vector = impl2.cpArbiterTotalImpulse(arbiterPtr)
  def totalImpulseWithFriction: Vector = impl2.cpArbiterTotalImpulseWithFriction(arbiterPtr)
}
