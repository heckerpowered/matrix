/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
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

infix fun PlayerEntity.attack(target: Entity) {
    attack(target)
}

infix fun Vec3d.distanceTo(other: Vec3d): Double {
    return distanceTo(other)
}

infix fun Vec3d.squaredDistanceTo(other: Vec3d): Double {
    return squaredDistanceTo(other)
}

infix fun Entity.distanceTo(other: Entity): Double {
    return pos.distanceTo(other.pos)
}

infix fun Entity.squaredDistanceTo(other: Entity): Double {
    return pos.squaredDistanceTo(other.pos)
}

infix fun Entity.distanceTo(other: Vec3d): Double {
    return pos.distanceTo(pos)
}

infix fun Entity.squaredDistanceTo(other: Vec3d): Double {
    return pos.distanceTo(pos)
}

fun Entity.getNearestEntities(distance: Double, filter: (Entity) -> Boolean = { true }): Entity? {
    return world
        .getOtherEntities(this, pos.toBox().expand(distance))
        .filter(filter)
        .minByOrNull { squaredDistanceTo(it) }
}