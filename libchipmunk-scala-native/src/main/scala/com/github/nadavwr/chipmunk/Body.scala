package com.github.nadavwr.chipmunk

import com.github.nadavwr.chipmunk.Body.{PositionHandler, VelocityHandler}

import scala.collection.mutable
import scala.language.implicitConversions
import scalanative.native
import scalanative.native._

object Body {
  type Value = CStruct0
  type Ptr = native.Ptr[Value]

  trait Implicits {
    implicit def bodyFromPtr(ptr: Body.Ptr): Body =
      if (ptr == null) null else allBodies(ptr.cast[Long])
  }
  object Implicits extends Implicits

  def apply(mass: Double = 0, moment: Double = 0): Body = {
    val bodyPtr = impl.cpBodyNew(mass, moment)
    val body = new Body(bodyPtr)
    allBodies += body.id -> body
    body
  }

  val allBodies: mutable.Map[Long, Body] = mutable.HashMap.empty

  type VelocityHandler = (Body, Vector, Double) => Unit
  val allVelocityHandlers: mutable.Map[Long, VelocityHandler] = mutable.HashMap.empty

  private val _velocityHandler: impl.cpBodyVelocityFunc =
    (bodyPtr: Body.Ptr, gx: CDouble, gy: CDouble, damping: CDouble, dt: CDouble) => {
      val body: Body = bodyPtr
      allVelocityHandlers(body.id).apply(body, Vector(gx, gy), dt)
    }
  private def putVelocityHandler(body: Body, velocityHandler: VelocityHandler): Unit = {
    allVelocityHandlers(body.id) = velocityHandler
    impl.cpBodySetVelocityUpdateFunc(body.ptr, _velocityHandler)
  }
  private def resetVelocityHandler(body: Body): Unit = {
    allVelocityHandlers -= body.id
    impl.cpBodySetVelocityUpdateFunc(body.ptr, impl.cpBodyUpdateVelocity _)
  }

  type PositionHandler = (Body, Double) => Unit
  val allPositionHandlers: mutable.Map[Long, PositionHandler] = mutable.HashMap.empty
  private val _positionHandler: impl.cpBodyPositionFunc =
    (bodyPtr: Body.Ptr, dt: CDouble) => {
      val body: Body = bodyPtr
      allPositionHandlers(body.id).apply(body, dt)
    }
  private def putPositionHandler(body: Body, positionHandler: PositionHandler): Unit = {
    allPositionHandlers(body.id) = positionHandler
    impl.cpBodySetPositionUpdateFunc(body.ptr, _positionHandler)
  }
  private def resetPositionHandler(body: Body): Unit = {
    allPositionHandlers -= body.id
    impl.cpBodySetPositionUpdateFunc(body.ptr, impl.cpBodyUpdatePosition _)
  }

  type PostStep = Body => Unit
  val postSteps: mutable.Map[Long, PostStep] = mutable.HashMap.empty
  val _postStep: impl.cpPostStepFunc =
    (spacePtr: Space.Ptr, key: native.Ptr[Byte], data: native.Ptr[Byte]) => {
      val body = Body.allBodies(data.cast[Long])
      val Some(postStep) = postSteps.remove(body.id)
      postStep(body)
    }

  def define(shapeDefs: ShapeDef*): Body = {
    val body = Body()
    shapeDefs.foreach(body.addShape)
    body
  }
}

class Body(val ptr: Body.Ptr) {
  private def bodyPtr: Body.Ptr = ptr
  def id: Long = bodyPtr.cast[Long]
  def space: Space = impl.cpBodyGetSpace(bodyPtr)
  def spaceOpt: Option[Space] = Option(space)
  def position: Vector = impl2.cpBodyGetPosition(bodyPtr)
  def position_=(value: Vector): Unit = impl2.cpBodySetPosition(bodyPtr, value.ptr)
  def addAt(space: Space, position: Vector): Body = {
    this.position = position
    space.addBody(this)
    this
  }
  def mass: Double = impl.cpBodyGetMass(bodyPtr)
  def moment: Double = impl.cpBodyGetMoment(bodyPtr)
  def centerOfGravity: Vector = impl2.cpBodyGetCenterOfGravity(bodyPtr)
  def centerOfGravity_=(value: Vector): Unit = impl2.cpBodySetCenterOfGravity(bodyPtr, value.ptr)
  def velocity: Vector = impl2.cpBodyGetVelocity(bodyPtr)
  def velocity_=(value: Vector): Unit = impl2.cpBodySetVelocity(bodyPtr, value.ptr)
  def force: Vector = impl2.cpBodyGetForce(bodyPtr)
  def force_=(value: Vector): Unit = impl2.cpBodySetForce(bodyPtr, value.ptr)
  def applyImpulse(impulse: Vector, pointOfImpact: Vector = Vector(0, 0), local: Boolean = true): Unit = {
    if (local) impl2.cpBodyApplyImpulseAtLocalPoint(bodyPtr, impulse.ptr, pointOfImpact.ptr)
    else impl2.cpBodyApplyImpulseAtWorldPoint(bodyPtr, impulse.ptr, pointOfImpact.ptr)
  }
  def angle: Double = impl.cpBodyGetAngle(bodyPtr)
  def angle_=(value: Double): Unit = impl.cpBodySetAngle(bodyPtr, value)
  def angularVelocity: Double = impl.cpBodyGetAngularVelocity(bodyPtr)
  def angularVelocity_=(value: Double): Unit = impl.cpBodySetAngularVelocity(bodyPtr, value)
  def torque: Double = impl.cpBodyGetTorque(bodyPtr)
  def torque_=(value: Double): Unit = impl.cpBodySetTorque(bodyPtr, value)
  def rotation: Vector = impl2.cpBodyGetRotation(bodyPtr)
  def toWorld(value: Vector): Vector = impl2.cpBodyLocalToWorld(bodyPtr, value.ptr)
  def toLocal(value: Vector): Vector = impl2.cpBodyWorldToLocal(bodyPtr, value.ptr)
  def dispose(): Unit = {
    shapes.foreach(_.dispose())
    spaceOpt.foreach(_ removeBody this)
    impl.cpBodyFree(bodyPtr)
    Body.allBodies -= id
  }

  def velocityHandler(handlerOpt: Option[Body.VelocityHandler]): Unit =
    handlerOpt match {
      case Some(handler) => Body.putVelocityHandler(this, handler)
      case None => Body.resetVelocityHandler(this)
    }

  val defaultVelocityHandler: VelocityHandler =
    (body: Body, gravity: Vector, dt: Double) => {
      impl.cpBodyUpdateVelocity(body.ptr, gravity.x, gravity.y, body.space.dampingFactor, dt)
    }
    
  def positionHandler(handlerOpt: Option[Body.PositionHandler]): Unit =
    handlerOpt match {
      case Some(handler) => Body.putPositionHandler(this, handler)
      case None => Body.resetPositionHandler(this)
    }

  val defaultPositionHandler: PositionHandler =
    (body: Body, dt: Double) => {
      impl.cpBodyUpdatePosition(body.ptr, dt)
    }

  def addShape(shapeDef: ShapeDef): Shape = {
    val shape = shapeDef match {
      case circle: CircleDef =>
        addCircle(circle.radius, circle.offset)
      case segment: SegmentDef =>
        addSegment(segment.a, segment.b, segment.radius)
      case polygon: PolygonDef =>
        addPolygon(polygon.vertices, polygon.transform, polygon.radius)
      case box: BoxDef =>
        addPolygon(Seq(Vector(-box.width/2, -box.height/2), Vector(-box.width/2, box.height/2),
                       Vector(box.width/2, box.height/2), Vector(box.width/2, -box.height/2)),
                   box.transform, box.radius)
    }

    shape.sensor = shapeDef.sensor
    shape.collisionType = shapeDef.collisionType
    shape.elasticity = shapeDef.elasticity
    shape.friction = shapeDef.friction
    shapeDef.massSpecOpt.foreach {
      case MassSpec.Mass(mass) => shape.mass = mass
      case MassSpec.Density(density) => shape.density = density
    }
    shape
  }

  def addCircle(radius: Double, offset: Vector = Vector(0, 0)): CircleShape = {
    val shapePtr = impl2.cpCircleShapeNew(bodyPtr, radius, offset.ptr)
    val shape = new CircleShape(shapePtr)
    shape.collisionType = 1.toULong
    Shape.allShapes += shape.id -> shape
    shapes += shape
    spaceOpt.foreach(_ addShape shape)
    shape
  }

  def addSegment(a: Vector, b: Vector, radius: Double): SegmentShape = {
    val shapePtr = impl2.cpSegmentShapeNew(bodyPtr, a.ptr, b.ptr, radius)
    val shape = new SegmentShape(shapePtr)
    shape.collisionType = 1.toULong
    Shape.allShapes += shape.id -> shape
    shapes += shape
    spaceOpt.foreach(_ addShape shape)
    shape
  }

  def addPolygon(verts: Seq[Vector], transform: Transform, radius: Double): PolygonShape = {
    val vertsArray = stackalloc[Vector.Value](verts.length)
    for (i <- verts.indices) (vertsArray + i) := verts(i)
    val shapePtr = impl2.cpPolyShapeNew(bodyPtr, verts.length, vertsArray, transform.ptr, radius)
    val shape = new PolygonShape(shapePtr)
    shape.collisionType = 1.toULong
    Shape.allShapes += shape.id -> shape
    shapes += shape
    spaceOpt.foreach(_ addShape shape)
    shape
  }

  def addPostStep(f: Body.PostStep): Unit = addPostStep(id, f)
  def addPostStep(key: Long, f: Body.PostStep): Unit = {
    val keyPtr = key.cast[native.Ptr[Byte]]
    val dataPtr = bodyPtr.cast[native.Ptr[Byte]]
    Body.postSteps(key) = f
    impl.cpSpaceAddPostStepCallback(space.ptr, Body._postStep, keyPtr, dataPtr)
  }

  private[chipmunk] var onBeginOpt: Option[Arbiter => Boolean] = None
  private[chipmunk] var onPreSolveOpt: Option[Arbiter => Boolean] = None
  private[chipmunk] var onPostSolveOpt: Option[Arbiter => Unit] = None
  private[chipmunk] var onSeparateOpt: Option[Arbiter => Unit] = None
  def onBegin(handler: Arbiter => Boolean): Unit = onBeginOpt = Some(handler)
  def onPreSolve(handler: Arbiter => Boolean): Unit = onPreSolveOpt = Some(handler)
  def onPostSolve(handler: Arbiter => Unit): Unit = onPostSolveOpt = Some(handler)
  def onSeparate(handler: Arbiter => Unit): Unit = onSeparateOpt = Some(handler)

  val shapes: mutable.Set[Shape] = mutable.HashSet.empty
}

class BodyDef()
