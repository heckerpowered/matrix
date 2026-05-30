/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

fun Vec3.toAABB(): AABB {
    return AABB(this, this)
}

operator fun Vec3.plus(other: Vec3): Vec3 {
    return add(other)
}

operator fun Vec3.minus(other: Vec3): Vec3 {
    return subtract(other)
}

operator fun Vec3.times(other: Vec3): Vec3 {
    return multiply(other)
}

operator fun Vec3.div(other: Vec3): Vec3 {
    return Vec3(x / other.x, y / other.y, z / other.z)
}

operator fun Vec3.plus(value: Double): Vec3 {
    return add(value, value, value)
}

operator fun Vec3.minus(value: Double): Vec3 {
    return subtract(value, value, value)
}

operator fun Vec3.times(value: Double): Vec3 {
    return scale(value)
}

operator fun Vec3.div(value: Double): Vec3 {
    return Vec3(x / value, y / value, z / value)
}