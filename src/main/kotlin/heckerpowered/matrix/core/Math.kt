package heckerpowered.matrix.core

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.projectionMatrix
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector2d
import org.joml.Vector3d
import org.joml.Vector4d
import org.joml.Vector4f
import java.lang.Math
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

fun Vec3d.toVector4f(): Vector4f {
    return Vector4f(x.toFloat(), y.toFloat(), z.toFloat(), 1.0F)
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

fun Vec3d.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

operator fun Vec3d.times(matrix: Matrix4f): Vector4d {
    return matrix.times(toVector4d())
}

operator fun Vector4f.times(scalar: Float): Vector4f {
    val result = Vector4f()
    mul(scalar, result)
    return result
}

operator fun Vector4f.times(matrix: Matrix4f): Vector4f {
    return matrix.transform(this, Vector4f())
}

operator fun Vector4f.timesAssign(scalar: Float) {
    mul(scalar)
}

operator fun Vector4f.timesAssign(matrix: Matrix4f) {
    mul(matrix)
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

fun worldToScreen(
    worldPosition: Vec3d,
    cameraPosition: Vec3d = minecraft.gameRenderer.camera.pos,
    cameraRotation: Quaternionf = minecraft.gameRenderer.camera.rotation,
    viewportWidth: Int = minecraft.window.scaledWidth,
    viewportHeight: Int = minecraft.window.scaledHeight,
): Vector2d? {
    val viewMatrix = Matrix4f()
        .rotate(cameraRotation.conjugate())
        .translate(
            -cameraPosition.x.toFloat(),
            -cameraPosition.y.toFloat(),
            -cameraPosition.z.toFloat(),
        )
    val viewProjectionMatrix = Matrix4f(projectionMatrix) * viewMatrix
    val clipSpacePosition = worldPosition.toVector4f() * viewProjectionMatrix
    if (clipSpacePosition.w <= 0.0F) {
        return null
    }

    val ndcX = clipSpacePosition.x / clipSpacePosition.w
    val ndcY = clipSpacePosition.y / clipSpacePosition.w
    if (ndcX < -1.0F || ndcX > 1.0F || ndcY < -1.0F || ndcY > 1.0F) {
        return null
    }

    val screenX = ((ndcX + 1.0F) / 2.0F) * viewportWidth
    val screenY = (1.0F - (ndcY + 1.0F) / 2.0F) * viewportHeight
    return Vector2d(screenX.toDouble(), screenY.toDouble())
}