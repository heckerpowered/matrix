package heckerpowered.matrix.core

import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector4d
import kotlin.math.abs

fun lerp(delta: Double, from: Double, to: Double): Double {
    return from + delta * (to - from)
}

fun inverseLerp(delta: Double, from: Double, to: Double): Double {
    return (delta - from) / (to - from)
}

fun Double.lerp(range: ClosedFloatingPointRange<Double>): Double {
    return lerp(this, range.start, range.endInclusive)
}

fun Double.inverseLerp(range: ClosedFloatingPointRange<Double>): Double {
    return inverseLerp(this, range.start, range.endInclusive)
}

fun Double.approximatelyEqual(other: Double, tolerance: Double = Double.MIN_VALUE): Boolean {
    return abs(this - other) <= tolerance
}

fun Float.approximatelyEqual(other: Float, tolerance: Float = Float.MIN_VALUE): Boolean {
    return abs(this - other) <= tolerance
}

fun wrapDegrees(degrees: Double): Double {
    var wrapDegrees = degrees % 360.0
    if (wrapDegrees >= 180.0) {
        wrapDegrees -= 360.0
    }

    if (wrapDegrees < -180.0f) {
        wrapDegrees += 360.0f
    }

    return wrapDegrees
}

fun toDegrees(radians: Double): Double {
    return Math.toDegrees(radians)
}

fun toRadians(degrees: Double): Double {
    return Math.toRadians(degrees)
}

fun Vec3d.toVector4d(): Vector4d {
    return Vector4d(x, y, z, 1.0)
}

fun Vector4d.toVec3d(): Vec3d {
    return Vec3d(x / w, y / w, z / w)
}

fun Vec3d.toMatrix4f(): Matrix4f {
    val matrix = Matrix4f()

    matrix.m00(x.toFloat())
    matrix.m01(y.toFloat())
    matrix.m02(z.toFloat())
    matrix.m03(1F)

    return matrix
}

operator fun Vec3d.times(matrix: Matrix4f): Vector4d {
    return matrix.times(toVector4d())
}

operator fun Matrix4f.times(vector: Vec3d): Vector4d {
    return times(vector.toVector4d())
}

operator fun Vector4d.times(matrix: Matrix4f): Vector4d {
    return matrix.times(this)
}

operator fun Matrix4f.times(vector: Vector4d): Vector4d {
    val x = m00() * vector.x + m10() * vector.y + m20() * vector.z + m30() * vector.w
    val y = m01() * vector.x + m11() * vector.y + m21() * vector.z + m31() * vector.w
    val z = m02() * vector.x + m12() * vector.y + m22() * vector.z + m32() * vector.w
    val w = m03() * vector.x + m13() * vector.y + m23() * vector.z + m33() * vector.w
    return Vector4d(x, y, z, w)
}

operator fun Matrix4f.times(other: Matrix4f): Matrix4f {
    return mul(other)
}