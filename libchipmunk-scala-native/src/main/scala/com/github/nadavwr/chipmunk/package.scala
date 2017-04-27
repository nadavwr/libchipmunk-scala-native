package com.github.nadavwr

import scala.scalanative.native
import scala.scalanative.native._
import com.github.nadavwr.ffi._

import scala.scalanative.runtime.GC

package object chipmunk
  extends BoundingBox.Implicits
  with Vector.Implicits
  with Space.Implicits
  with Body.Implicits
  with Shape.Implicits
  with Transform.Implicits
  with CollisionHandler.Implicits {

  @link("chipmunk")
  @extern
  object impl {
    def cpSpaceNew(): Space.Ptr = extern
    def cpSpaceFree(space: Space.Ptr): Unit = extern
    def cpSpaceGetIterations(space: Space.Ptr): CInt = extern
    def cpSpaceSetIterations(space: Space.Ptr, value: CInt): Unit = extern
    def cpSpaceSetGravity(space: Space.Ptr, gravity: Ptr[CArray[CDouble, Nat._2]]): Unit = extern
    def cpSpaceGetDamping(space: Space.Ptr): CDouble = extern
    def cpSpaceSetDamping(space: Space.Ptr, value: CDouble): Unit = extern
    def cpSpaceGetCurrentTimeStep(space: Space.Ptr): CDouble = extern
    def cpSpaceStep(space: Space.Ptr, dt: CDouble): Unit = extern
    def cpSpaceAddBody(space: Space.Ptr, body: Body.Ptr): Body.Ptr = extern
    def cpSpaceRemoveBody(space: Space.Ptr, body: Body.Ptr): Unit = extern
    def cpSpaceAddShape(space: Space.Ptr, shape: Shape.Ptr): Body.Ptr = extern
    def cpSpaceRemoveShape(space: Space.Ptr, shape: Shape.Ptr): Unit = extern
    def cpBodyGetSpace(body: Body.Ptr): Space.Ptr = extern
    def cpBodyNew(mass: CDouble, moment: CDouble): Body.Ptr = extern
    def cpBodyFree(body: Body.Ptr): Unit = extern
    def cpBodyGetMoment(body: Body.Ptr): CDouble = extern
    def cpBodyGetMass(body: Body.Ptr): CDouble = extern
    def cpBodyGetAngle(body: Body.Ptr): CDouble = extern
    def cpBodySetAngle(body: Body.Ptr, value: CDouble): Unit = extern
    def cpBodyGetAngularVelocity(body: Body.Ptr): CDouble = extern
    def cpBodySetAngularVelocity(body: Body.Ptr, value: CDouble): Unit = extern
    def cpBodyGetTorque(body: Body.Ptr): CDouble = extern
    def cpBodySetTorque(body: Body.Ptr, value: CDouble): Unit = extern
    type cpBodyShapeIteratorFunc = CFunctionPtr3[Body.Ptr, Shape.Ptr, Ptr[Byte], Unit]
    def cpBodyEachShape(body: Body.Ptr, f: cpBodyShapeIteratorFunc, data: Ptr[Byte]): Unit = extern
    type cpBodyVelocityFunc = CFunctionPtr5[Body.Ptr, Double, Double, Double, Double, Unit]
    def cpBodySetVelocityUpdateFunc(bodyPtr: Body.Ptr, f: cpBodyVelocityFunc): Unit = extern
    def cpBodyUpdateVelocity(bodyPtr: Body.Ptr, gx: CDouble, gy: CDouble, damping: CDouble, dt: CDouble): Unit = extern
    type cpBodyPositionFunc = CFunctionPtr2[Body.Ptr, CDouble, Unit]
    def cpBodySetPositionUpdateFunc(body: Body.Ptr, f: cpBodyPositionFunc): Unit = extern
    def cpBodyUpdatePosition(bodyPtr: Body.Ptr, dt: CDouble): Unit = extern
    def cpShapeGetBody(shape: Shape.Ptr): Body.Ptr = extern
    def cpShapeSetBody(shape: Shape.Ptr, body: Body.Ptr): Unit = extern
    def cpShapeGetCollisionType(shape: Shape.Ptr): cpCollisionType = extern
    def cpShapeSetCollisionType(shape: Shape.Ptr, value: cpCollisionType): Unit = extern
    def cpShapeGetSensor(shape: Shape.Ptr): CInt = extern
    def cpShapeSetSensor(shape: Shape.Ptr, value: CInt): Unit = extern
    def cpShapeGetElasticity(shape: Shape.Ptr): CDouble = extern
    def cpShapeSetElasticity(shape: Shape.Ptr, value: CDouble): Unit = extern
    def cpShapeGetFriction(shape: Shape.Ptr): CDouble = extern
    def cpShapeSetFriction(shape: Shape.Ptr, value: CDouble): Unit = extern
    def cpShapeGetMass(shapePtr: Shape.Ptr): CDouble = extern
    def cpShapeSetMass(shapePtr: Shape.Ptr, mass: CDouble): Unit = extern
    def cpShapeGetDensity(shapePtr: Shape.Ptr): CDouble = extern
    def cpShapeSetDensity(shapePtr: Shape.Ptr, density: CDouble): Unit = extern
    def cpShapeGetArea(shapePtr: Shape.Ptr): CDouble = extern
    def cpShapeGetMoment(shapePtr: Shape.Ptr): CDouble = extern
    def cpCircleShapeGetRadius(shapePtr: Shape.Ptr): CDouble = extern
    def cpSegmentShapeGetRadius(shapePtr: Shape.Ptr): CDouble = extern
    def cpPolyShapeGetRadius(shapePtr: Shape.Ptr): CDouble = extern
    def cpPolyShapeGetCount(shapePtr: Shape.Ptr): CInt = extern
    def cpShapeGetSpace(shape: Shape.Ptr): Space.Ptr = extern
    def cpShapeFree(shape: Shape.Ptr): Unit = extern

    def cpMomentForBox(m: CDouble, width: CDouble, height: CDouble): CDouble = extern
    def cpAreaForCircle(r1: CDouble, r2: CDouble): CDouble = extern

    def cpArbiterIsFirstContact(arbiter: Ptr[Arbiter.Value]): CInt = extern
    def cpArbiterIsRemoval(arbiter: Ptr[Arbiter.Value]): CInt = extern
    def cpArbiterGetShapes(arbiter: Ptr[Arbiter.Value], a: Ptr[Shape.Ptr], b: Ptr[Shape.Ptr]): Unit = extern
    def cpArbiterGetBodies(arbiter: Ptr[Arbiter.Value], a: Ptr[Body.Ptr], b: Ptr[Body.Ptr]): Unit = extern
    def cpArbiterCallWildcardBeginA(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): CInt = extern
    def cpArbiterCallWildcardBeginB(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): CInt = extern
    def cpArbiterCallWildcardPreSolveA(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): CInt = extern
    def cpArbiterCallWildcardPreSolveB(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): CInt = extern
    def cpArbiterCallWildcardPostSolveA(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): Unit = extern
    def cpArbiterCallWildcardPostSolveB(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): Unit = extern
    def cpArbiterCallWildcardSeparateA(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): Unit = extern
    def cpArbiterCallWildcardSeparateB(arbiter: Ptr[Arbiter.Value], space: Space.Ptr): Unit = extern

    type cpCollisionType = ULong
    type cpCollisionBeginFunc = CFunctionPtr3[Ptr[Arbiter.Value], Space.Ptr, Ptr[Byte], CInt]
    type cpCollisionPreSolveFunc = CFunctionPtr3[Ptr[Arbiter.Value], Space.Ptr, Ptr[Byte], CInt]
    type cpCollisionPostSolveFunc = CFunctionPtr3[Ptr[Arbiter.Value], Space.Ptr, Ptr[Byte], Unit]
    type cpCollisionSeparateFunc = CFunctionPtr3[Ptr[Arbiter.Value], Space.Ptr, Ptr[Byte], Unit]
    type cpCollisionHandler = CStruct7[cpCollisionType, cpCollisionType, cpCollisionBeginFunc, cpCollisionPreSolveFunc, cpCollisionPostSolveFunc, cpCollisionSeparateFunc, Ptr[Byte]]
    def cpSpaceAddCollisionHandler(space: Space.Ptr, a: cpCollisionType, b: cpCollisionType): Ptr[cpCollisionHandler] = extern
    def cpSpaceAddWildcardHandler(space: Space.Ptr, collisionType: cpCollisionType): Ptr[cpCollisionHandler] = extern
    def cpSpaceAddDefaultCollisionHandler(space: Space.Ptr): Ptr[cpCollisionHandler] = extern

    type cpPostStepFunc = CFunctionPtr3[Space.Ptr, Ptr[Byte], Ptr[Byte], Unit]
    def cpSpaceAddPostStepCallback(space: Space.Ptr, f: cpPostStepFunc, key: Ptr[Byte], data: Ptr[Byte]): CInt = extern
  }

  object impl2 {
    private val module = Module.open("libchipmunk.dylib")

    object cpMomentForCircle {
      private val call = module.prepare[CDouble, CDouble, CDouble, Vector.Value, CDouble]("cpMomentForCircle")
      def apply(m: CDouble, r1: CDouble, r2: CDouble, offsetPtr: Vector.Ptr): CDouble = {
        val mPtr = stackalloc[CDouble]; !mPtr = m
        val r1Ptr = stackalloc[CDouble]; !r1Ptr = r1
        val r2Ptr = stackalloc[CDouble]; !r2Ptr = r2
        val momentPtr = stackalloc[CDouble]
        call(mPtr, r1Ptr, r2Ptr, offsetPtr)(momentPtr)
        !momentPtr
      }
    }

    object cpMomentForSegment {
      private val call = module.prepare[CDouble, Vector.Value, Vector.Value, CDouble, CDouble]("cpMomentForSegment")
      def apply(m: Double, aPtr: Vector.Ptr, bPtr: Vector.Ptr, radius: Double): Double = {
        val mPtr = stackalloc[CDouble]; !mPtr = m
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val momentPtr = stackalloc[CDouble]
        call(mPtr, aPtr, bPtr, radiusPtr)(momentPtr)
        !momentPtr
      }
    }

    object cpAreaForSegment {
      private val call = module.prepare[Vector.Value, Vector.Value, CDouble, CDouble]("cpAreaForSegment")
      def apply(aPtr: Vector.Ptr, bPtr: Vector.Ptr, radius: Double): Double = {
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val areaPtr = stackalloc[CDouble]
        call(aPtr, bPtr, radiusPtr)(areaPtr)
        !areaPtr
      }
    }

    object cpMomentForPoly {
      private val call = module.prepare[CDouble, CInt, Ptr[Vector.Value], Vector.Value, CDouble, CDouble]("cpMomentForPoly")
      def apply(m: CDouble, numVerts: CInt, vertsPtr: Vector.Ptr, offsetPtr: Vector.Ptr, radius: CDouble): CDouble = {
        val mPtr = stackalloc[CDouble]; !mPtr = m
        val numVertsPtr = stackalloc[CInt]; !numVertsPtr = numVerts
        val vertsPtrPtr = stackalloc[Vector.Ptr]; !vertsPtrPtr = vertsPtr
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val momentPtr = stackalloc[CDouble]
        call(mPtr, numVertsPtr, vertsPtrPtr, offsetPtr, radiusPtr)(momentPtr)
        !momentPtr
      }
    }

    object cpAreaForPoly {
      private val call = module.prepare[CInt, Ptr[Vector.Value], CDouble, CDouble]("cpAreaForPoly")
      def apply(numVerts: CInt, vertsPtr: Vector.Ptr, radius: CDouble): CDouble = {
        val numVertsPtr = stackalloc[CInt]; !numVertsPtr = numVerts
        val vertsPtrPtr = stackalloc[Vector.Ptr]; !vertsPtrPtr = vertsPtr
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val areaPtr = stackalloc[CDouble]
        call(numVertsPtr, vertsPtrPtr, radiusPtr)(areaPtr)
        !areaPtr
      }
    }

    object cpCentroidForPoly {
      private val call = module.prepare[CInt, Ptr[Vector.Value], Vector.Value]("cpCentroidForPoly")
      def apply(numVerts: CInt, vertsPtr: Vector.Ptr, radius: CDouble): Vector = {
        val numVertsPtr = stackalloc[CInt]; !numVertsPtr = numVerts
        val vertsPtrPtr = stackalloc[Vector.Ptr]; !vertsPtrPtr = vertsPtr
        val centroidPtr = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(numVertsPtr, vertsPtrPtr)(centroidPtr)
        new Vector(centroidPtr)
      }
    }

    abstract class GetSpaceVectorCall(symbol: String) {
      private val call = module.prepare[Space.Ptr, Vector.Value](symbol)
      def apply(space: Space): Vector = {
        val spacePtrPtr = stackalloc[Space.Ptr]; !spacePtrPtr = space.ptr
        val vectorPtr = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(spacePtrPtr)(vectorPtr)
        new Vector(vectorPtr)
      }
    }
    object cpSpaceGetGravity extends GetSpaceVectorCall("cpSpaceGetGravity")

    abstract class GetBodyVectorCall(symbol: String) {
      val call = module.prepare[Body.Ptr, Vector.Value](symbol)
      def apply(bodyPtr: Body.Ptr): Vector = {
        val bodyPtrPtr = stackalloc[Body.Ptr]; !bodyPtrPtr = bodyPtr
        val vectorPtr = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(bodyPtrPtr)(vectorPtr)
        new Vector(vectorPtr)
      }
    }
    abstract class SetBodyVectorCall(symbol: String) {
      val call = module.prepare[Body.Ptr, Vector.Value, Unit](symbol)
      def apply(body: Body.Ptr, vector: Vector): Unit = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        call(bodyPtr, vector.ptr)(stackalloc[Unit])
      }
    }
    object cpBodyGetPosition extends GetBodyVectorCall("cpBodyGetPosition")
    object cpBodySetPosition extends SetBodyVectorCall("cpBodySetPosition")
    object cpBodyGetCenterOfGravity extends GetBodyVectorCall("cpBodyGetCenterOfGravity")
    object cpBodySetCenterOfGravity extends SetBodyVectorCall("cpBodySetCenterOfGravity")
    object cpBodyGetVelocity extends GetBodyVectorCall("cpBodyGetVelocity")
    object cpBodySetVelocity extends SetBodyVectorCall("cpBodySetVelocity")
    object cpBodyGetForce extends GetBodyVectorCall("cpBodyGetForce")
    object cpBodySetForce extends SetBodyVectorCall("cpBodySetForce")
    object cpBodyGetRotation extends GetBodyVectorCall("cpBodyGetRotation")

    abstract class BodyApplyVector2Call(symbol: String) {
      val call = module.prepare[Body.Ptr, Vector.Value, Vector.Value, Unit](symbol)
      def apply(body: Body.Ptr, v1: Vector.Ptr, v2: Vector.Ptr): Unit = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        call(bodyPtr, v1, v2)(stackalloc[Unit])
      }
    }
    object cpBodyApplyForceAtWorldPoint extends BodyApplyVector2Call("cpBodyApplyForceAtWorldPoint")
    object cpBodyApplyForceAtLocalPoint extends BodyApplyVector2Call("cpBodyApplyForceAtLocalPoint")
    object cpBodyApplyImpulseAtWorldPoint extends BodyApplyVector2Call("cpBodyApplyImpulseAtWorldPoint")
    object cpBodyApplyImpulseAtLocalPoint extends BodyApplyVector2Call("cpBodyApplyImpulseAtLocalPoint")

    abstract class BodyVectorTransform(symbol: String) {
      val call = module.prepare[Body.Ptr, Vector.Value, Vector.Value](symbol)
      def apply(body: Body.Ptr, vector: Vector.Ptr): Vector.Ptr = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        val resultPtr = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(bodyPtr, vector)(resultPtr)
        resultPtr
      }
    }
    object cpBodyLocalToWorld extends BodyVectorTransform("cpBodyLocalToWorld")
    object cpBodyWorldToLocal extends BodyVectorTransform("cpBodyWorldToLocal")

    object cpShapeGetBB {
      val call = module.prepare[Shape.Ptr, BoundingBox.Value]("cpShapeGetBB")
      def apply(shape: Shape.Ptr): BoundingBox.Ptr = {
        val shapePtrPtr = stackalloc[Shape.Ptr]; !shapePtrPtr = shape
        val boundingBoxPtr = GC.malloc_atomic(sizeof[BoundingBox.Value]).cast[BoundingBox.Ptr]
        call(shapePtrPtr)(boundingBoxPtr)
        boundingBoxPtr
      }
    }

    object cpCircleShapeNew {
      val call = module.prepare[Body.Ptr, CDouble, Vector.Value, Shape.Ptr]("cpCircleShapeNew")
      def apply(body: Body.Ptr, radius: CDouble, offset: Vector.Ptr): Shape.Ptr = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val resultPtr = GC.malloc_atomic(sizeof[Shape.Ptr]).cast[Ptr[Shape.Ptr]]
        call(bodyPtr, radiusPtr, offset)(resultPtr)
        !resultPtr
      }
    }

    object cpSegmentShapeNew {
      val call = module.prepare[Body.Ptr, Vector.Value, Vector.Value, CDouble, Shape.Ptr]("cpSegmentShapeNew")
      def apply(body: Body.Ptr, a: Vector.Ptr, b: Vector.Ptr, radius: CDouble): Shape.Ptr = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val resultPtr = GC.malloc_atomic(sizeof[Shape.Ptr]).cast[Ptr[Shape.Ptr]]
        call(bodyPtr, a, b, radiusPtr)(resultPtr)
        !resultPtr
      }
    }

    object cpPolyShapeNew {
      val call = module.prepare[Body.Ptr, CInt, Vector.Ptr, Transform.Value, CDouble, Shape.Ptr]("cpPolyShapeNew")
      def apply(body: Body.Ptr, numVerts: CInt, verts: Vector.Ptr, transform: Transform.Ptr, radius: CDouble): Shape.Ptr = {
        val bodyPtr = stackalloc[Body.Ptr]; !bodyPtr = body
        val numVertsPtr = stackalloc[CInt]; !numVertsPtr = numVerts
        val vertsPtrPtr = stackalloc[Vector.Ptr]; !vertsPtrPtr = verts
        val radiusPtr = stackalloc[CDouble]; !radiusPtr = radius
        val resultPtr = GC.malloc_atomic(sizeof[Shape.Ptr]).cast[Ptr[Shape.Ptr]]
        call(bodyPtr, numVertsPtr, vertsPtrPtr, transform, radiusPtr)(resultPtr)
        !resultPtr
      }
    }

    abstract class GetShapeVectorCall(symbol: String) {
      val call = module.prepare[Shape.Ptr, Vector.Value](symbol)
      def apply(shape: Shape.Ptr): Vector.Ptr = {
        val shapePtr = stackalloc[Shape.Ptr]; !shapePtr = shape
        val vector = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(shapePtr)(vector)
        vector
      }
    }
    abstract class SetShapeVectorCall(symbol: String) {
      val call = module.prepare[Shape.Ptr, Vector.Value, Unit](symbol)
      def apply(shape: Shape.Ptr, vector: Vector.Ptr): Unit = {
        val shapePtr = stackalloc[Shape.Ptr]; !shapePtr = shape
        call(shapePtr, vector)(stackalloc[Unit])
      }
    }

    object cpShapeGetSurfaceVelocity extends GetShapeVectorCall("cpShapeGetSurfaceVelocity")
    object cpShapeSetSurfaceVelocity extends SetShapeVectorCall("cpShapeSetSurfaceVelocity")
    object cpCircleShapeGetOffset extends GetShapeVectorCall("cpCircleShapeGetOffset")
    object cpSegmentShapeGetA extends GetShapeVectorCall("cpSegmentShapeGetA")
    object cpSegmentShapeGetB extends GetShapeVectorCall("cpSegmentShapeGetB")
    object cpSegmentShapeGetNormal extends GetShapeVectorCall("cpSegmentShapeGetNormal")
    object cpShapeGetCenterOfGravity extends GetShapeVectorCall("cpShapeGetCenterOfGravity")

    object cpPolyShapeGetVert {
      val call = module.prepare[Shape.Ptr, CInt, Vector.Value]("cpPolyShapeGetVert")
      def apply(shape: Shape.Ptr, i: CInt): Vector.Ptr = {
        val shapePtr = stackalloc[Shape.Ptr]; !shapePtr = shape
        val iPtr = stackalloc[CInt]; !iPtr = i
        val vectorPtr = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(shapePtr, iPtr)(vectorPtr)
        vectorPtr
      }
    }

    abstract class GetArbiterVectorCall(symbol: String) {
      val call = module.prepare[Ptr[Arbiter.Value], Vector.Value](symbol)
      def apply(arbiterPtr: Ptr[Arbiter.Value]): Vector.Ptr = {
        val arbiterPtrPtr = stackalloc[Ptr[Arbiter.Value]]; !arbiterPtrPtr = arbiterPtr
        val vector = GC.malloc_atomic(sizeof[Vector.Value]).cast[Vector.Ptr]
        call(arbiterPtrPtr)(vector)
        vector
      }
    }

    object cpArbiterTotalImpulseWithFriction extends GetArbiterVectorCall("cpArbiterTotalImpulseWithFriction")
    object cpArbiterTotalImpulse extends GetArbiterVectorCall("cpArbiterTotalImpulse")
  }

}
