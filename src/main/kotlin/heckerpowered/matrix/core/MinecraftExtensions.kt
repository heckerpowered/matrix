package heckerpowered.matrix.core

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

fun Vec3d.toBox(): Box {
    return Box(this, this)
}

operator fun Vec3d.plus(other: Vec3d): Vec3d {
    return add(other)
}

operator fun Vec3d.minus(other: Vec3d): Vec3d {
    return subtract(other)
}

operator fun Vec3d.times(other: Vec3d): Vec3d {
    return multiply(other)
}

operator fun Vec3d.div(other: Vec3d): Vec3d {
    return Vec3d(x / other.x, y / other.y, z / other.z)
}

operator fun Vec3d.plus(value: Double): Vec3d {
    return add(value, value, value)
}

operator fun Vec3d.minus(value: Double): Vec3d {
    return subtract(value, value, value)
}

operator fun Vec3d.times(value: Double): Vec3d {
    return multiply(value)
}

operator fun Vec3d.div(value: Double): Vec3d {
    return Vec3d(x / value, y / value, z / value)
}