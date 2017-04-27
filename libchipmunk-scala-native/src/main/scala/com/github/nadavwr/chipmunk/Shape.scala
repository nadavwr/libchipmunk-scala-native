package com.github.nadavwr.chipmunk

import scala.collection.mutable
import scala.language.{implicitConversions, reflectiveCalls}
import scalanative.native
import scalanative.native._

object Shape {
  type Value = CStruct0
  type Ptr = native.Ptr[Value]

  val allShapes: mutable.Map[Long, Shape] = mutable.HashMap.empty

  trait Implicits {
    implicit def shapeFromPtr(shapePtr: Shape.Ptr): Shape =
      if (shapePtr == null) null else allShapes(shapePtr.cast[Long])
  }
  object Implicits extends Implicits
}

class Shape private[chipmunk] (val ptr: Shape.Ptr) {
  private def shapePtr: Shape.Ptr = ptr
  def id: Long = shapePtr.cast[Long]
  def body: Body = impl.cpShapeGetBody(shapePtr)
  def bodyOpt: Option[Body] = Option(body)
  def body_=(value: Body): Unit = {
    val bodyPtr = if (value == null) null else value.ptr
    spaceOpt.foreach(_ removeShape this)
    bodyOpt.foreach(_.shapes -= this)
    impl.cpShapeSetBody(shapePtr, bodyPtr)
    bodyOpt.foreach(_.shapes += this)
    bodyOpt.flatMap(_.spaceOpt).foreach(_ addShape this)
  }
  def boundingBox: BoundingBox = impl2.cpShapeGetBB(shapePtr)
  def surfaceVelocity: Vector = impl2.cpShapeGetSurfaceVelocity(shapePtr)
  def surfaceVelocity_=(value: Vector): Unit = impl2.cpShapeSetSurfaceVelocity(shapePtr, value.ptr)
  def space: Space = impl.cpShapeGetSpace(shapePtr)
  def spaceOpt: Option[Space] = Option(space)
  def sensor: Boolean = impl.cpShapeGetSensor(shapePtr) != 0
  def sensor_=(value: Boolean): Unit = impl.cpShapeSetSensor(shapePtr, if (value) 1 else 0)
  def elasticity: Double = impl.cpShapeGetElasticity(shapePtr)
  def elasticity_=(value: Double): Unit = impl.cpShapeSetElasticity(shapePtr, value)
  def friction: Double = impl.cpShapeGetFriction(shapePtr)
  def friction_=(value: Double): Unit = impl.cpShapeSetFriction(shapePtr, value)
  def collisionType: impl.cpCollisionType = impl.cpShapeGetCollisionType(shapePtr)
  def collisionType_=(value: impl.cpCollisionType): Unit = impl.cpShapeSetCollisionType(shapePtr, value)
  def mass: Double = impl.cpShapeGetMass(shapePtr)
  def mass_=(mass: Double): Unit = impl.cpShapeSetMass(shapePtr, mass)
  def density: Double = impl.cpShapeGetDensity(shapePtr)
  def density_=(density: Double): Unit = impl.cpShapeSetDensity(shapePtr, density)
  def centroid: Vector = impl2.cpShapeGetCenterOfGravity(shapePtr)
  def area: Double = impl.cpShapeGetArea(shapePtr)
  def moment: Double = impl.cpShapeGetMoment(shapePtr)
  def dispose(): Unit = {
    spaceOpt.foreach(_ removeShape this)
    if (bodyOpt.isDefined) body = null
    impl.cpShapeFree(shapePtr)
    Shape.allShapes -= id
  }
}

sealed trait MassSpec
object MassSpec {
  case class Mass(mass: Double) extends MassSpec
  case class Density(density: Double) extends MassSpec
}

abstract class ShapeDef(val massSpecOpt: Option[MassSpec],
                        val sensor: Boolean,
                        val friction: Double,
                        val elasticity: Double,
                        val collisionType: ULong) {
  type Repr <: ShapeDef
  def offsetBy(offset: Vector): Repr
  def rotateBy(theta: Double): Repr
  def centroid: Vector
  def area: Double
  def massOpt: Option[Double] = massSpecOpt.map[Double] {
    case MassSpec.Mass(mass) => mass
    case MassSpec.Density(density) => density * area
  }
  def momentOpt: Option[Double]
  def withDensity(density: Double): Repr
  def withMass(mass: Double): Repr
}

case class CircleDef(radius: Double,
                     override val massSpecOpt: Option[MassSpec] = Some(MassSpec.Density(1)),
                     override val sensor: Boolean = false,
                     override val friction: Double = 0,
                     override val elasticity: Double = 1.0,
                     override val collisionType: ULong = 1.toULong,
                     offset: Vector = Vector.zero)
  extends ShapeDef(massSpecOpt, sensor, friction, elasticity, collisionType) {
  override type Repr = CircleDef
  override def offsetBy(offset: Vector): CircleDef =
    copy(offset = this.offset + offset)
  override def rotateBy(theta: Double): CircleDef = this
  override def centroid: Vector = offset
  override lazy val area: Double = impl.cpAreaForCircle(0, radius)
  override lazy val momentOpt: Option[Double] = massOpt.map { mass =>
    impl2.cpMomentForCircle(mass, 0, radius, offset.ptr)
  }

  override def withDensity(density: CDouble): CircleDef = copy(massSpecOpt = Some(MassSpec.Density(density)))
  override def withMass(mass: CDouble): CircleDef = copy(massSpecOpt = Some(MassSpec.Mass(mass)))
}

case class SegmentDef(a: Vector, b: Vector, radius: Double = 0,
                      override val massSpecOpt: Option[MassSpec] = Some(MassSpec.Density(1)),
                      override val sensor: Boolean = false,
                      override val friction: Double = 0,
                      override val elasticity: Double = 1.0,
                      override val collisionType: ULong = 1.toULong)
  extends ShapeDef(massSpecOpt, sensor, friction, elasticity, collisionType) {
  override type Repr = SegmentDef
  override def offsetBy(offset: Vector): SegmentDef =
    copy(a = a + offset, b = b + offset)
  override lazy val centroid: Vector = a + ((a - b)/2)
  override def rotateBy(theta: Double): SegmentDef = {
    val b0 = b - centroid
    val rotate = Transform.rotate(theta)
    copy(a = rotate(a - centroid) + centroid,
         b = rotate(b - centroid) + centroid)
  }
  override lazy val area: Double = impl2.cpAreaForSegment(a.ptr, b.ptr, radius)
  override lazy val momentOpt: Option[Double] = massOpt.map { mass =>
    impl2.cpMomentForSegment(mass, a.ptr, b.ptr, radius)
  }
  override def withDensity(density: CDouble): SegmentDef = copy(massSpecOpt = Some(MassSpec.Density(density)))
  override def withMass(mass: CDouble): SegmentDef = copy(massSpecOpt = Some(MassSpec.Mass(mass)))
}

case class PolygonDef(vertices: Seq[Vector], radius: Double = 0,
                      override val massSpecOpt: Option[MassSpec] = Some(MassSpec.Density(1)),
                      override val sensor: Boolean = false,
                      override val friction: Double = 0,
                      override val elasticity: Double = 1.0,
                      override val collisionType: ULong = 1.toULong,
                      transform: Transform = Transform.identity)
  extends ShapeDef(massSpecOpt, sensor, friction, elasticity, collisionType) {

  override type Repr = PolygonDef

  override def offsetBy(offset: Vector): PolygonDef =
    copy(transform = transform compose Transform.translate(offset))

  override def rotateBy(theta: Double): PolygonDef =
    copy(transform = transform compose Transform.rotate(theta))

  private lazy val withVerticesImpl: (Vector.Ptr => Any) => Any = {
    val verticesPtr = stackalloc[Vector.Value](vertices.length)
    for (i <- vertices.indices) (verticesPtr + i) := vertices(i)

    f => f(verticesPtr)
  }

  private def withVertices[A](f: Vector.Ptr => A): A = {
    withVerticesImpl(f.asInstanceOf[Vector.Ptr => Any]).asInstanceOf[A]
  }

  override lazy val centroid: Vector =
    withVertices { verticesPtr =>
      impl2.cpCentroidForPoly(vertices.length, verticesPtr, radius)
    }

  override lazy val area: CDouble =
    withVertices { verticesPtr =>
      impl2.cpAreaForPoly(vertices.length, verticesPtr, radius)
    }

  override def momentOpt: Option[CDouble] = massOpt.map { mass =>
    withVertices { verticesPtr =>
      impl2.cpMomentForPoly(mass, vertices.length, verticesPtr, transform.offset.ptr, radius)
    }
  }

  override def withDensity(density: CDouble): PolygonDef = copy(massSpecOpt = Some(MassSpec.Density(density)))
  override def withMass(mass: CDouble): PolygonDef = copy(massSpecOpt = Some(MassSpec.Mass(mass)))
}

case class BoxDef(width: Double, height: Double, radius: Double = 0,
                  override val massSpecOpt: Option[MassSpec] = Some(MassSpec.Density(1)),
                  override val sensor: Boolean = false,
                  override val friction: Double = 0,
                  override val elasticity: Double = 1.0,
                  override val collisionType: ULong = 1.toULong,
                  transform: Transform = Transform.identity)
  extends ShapeDef(massSpecOpt, sensor, friction, elasticity, collisionType) {

  override type Repr = BoxDef

  override def offsetBy(offset: Vector): BoxDef =
    copy(transform = transform compose Transform.translate(offset))

  override def rotateBy(theta: Double): BoxDef =
    copy(transform = transform compose Transform.rotate(theta))

  override lazy val centroid: Vector = transform.offset
  override lazy val area: Double = width * height
  override lazy val momentOpt: Option[Double] = massOpt.map { mass =>
    impl.cpMomentForBox(mass, width, height)
  }
  override def withDensity(density: CDouble): BoxDef = copy(massSpecOpt = Some(MassSpec.Density(density)))
  override def withMass(mass: CDouble): BoxDef = copy(massSpecOpt = Some(MassSpec.Mass(mass)))
}

class CircleShape private[chipmunk] (shapePtr: Shape.Ptr) extends Shape(shapePtr) {
  def radius: Double = impl.cpCircleShapeGetRadius(shapePtr)
  def offset: Vector = impl2.cpCircleShapeGetOffset(shapePtr)
}

class SegmentShape private[chipmunk] (shapePtr: Shape.Ptr) extends Shape(shapePtr) {
  def radius: Double = impl.cpSegmentShapeGetRadius(shapePtr)
  def a: Vector = impl2.cpSegmentShapeGetA(shapePtr)
  def b: Vector = impl2.cpSegmentShapeGetB(shapePtr)
  def normal: Vector = impl2.cpSegmentShapeGetNormal(shapePtr)
}

class PolygonShape private[chipmunk] (shapePtr: Shape.Ptr) extends Shape(shapePtr) {
  def radius: Double = impl.cpPolyShapeGetRadius(shapePtr)

  class Vertices extends collection.IndexedSeq[Vector]
    with collection.IndexedSeqOptimized[Vector, IndexedSeq[Vector]] {
    override def length: CInt = impl.cpPolyShapeGetCount(shapePtr)
    override def apply(i: CInt): Vector =
      impl2.cpPolyShapeGetVert(shapePtr, i)
  }
  def vertices: Vertices = new Vertices
}
