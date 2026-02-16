/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extension

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.floor
import kotlin.random.Random

object BoxExtension {
    private fun hashToUnit(value: Long): Double {
        var x = value
        x = x xor (x shl 21)
        x = x xor (x ushr 35)
        x = x xor (x shl 4)
        return (x and 0xFFFFFFFFL).toDouble() / 0xFFFFFFFFL
    }

    private fun smoothNoise1D(value: Double): Double {
        val left = floor(value).toLong()
        val right = left + 1

        val local = value - left
        val smooth = local * local * (3.0 - 2.0 * local)

        val leftValue = hashToUnit(left)
        val rightValue = hashToUnit(right)

        return leftValue + (rightValue - leftValue) * smooth
    }

    fun Box.sample(value: Double, jitterStrength: Double = 0.0): Vec3d {
        val baseX = minX + (maxX - minX) * smoothNoise1D(value + 0.0)
        val baseY = minY + (maxY - minY) * smoothNoise1D(value + 100.0)
        val baseZ = minZ + (maxZ - minZ) * smoothNoise1D(value + 200.0)

        val maxOffsetX = (maxX - minX) * jitterStrength
        val maxOffsetY = (maxY - minY) * jitterStrength
        val maxOffsetZ = (maxZ - minZ) * jitterStrength

        val offsetX = Random.nextDouble(-maxOffsetX, maxOffsetX)
        val offsetY = Random.nextDouble(-maxOffsetY, maxOffsetY)
        val offsetZ = Random.nextDouble(-maxOffsetZ, maxOffsetZ)

        return clamp(
            Vec3d(
                baseX + offsetX,
                baseY + offsetY,
                baseZ + offsetZ
            )
        )
    }

    private fun Box.clamp(position: Vec3d): Vec3d {
        return Vec3d(
            position.x.coerceIn(minX, maxX),
            position.y.coerceIn(minY, maxY),
            position.z.coerceIn(minZ, maxZ)
        )
    }

    fun Box.random(): Vec3d {
        val x = Random.nextDouble(minX, maxX)
        val y = Random.nextDouble(minY, maxY)
        val z = Random.nextDouble(minZ, maxZ)
        return Vec3d(x, y, z)
    }

    fun Box.scale(percent: Double): Box {
        val center = this.center

        val minX = center.x - lengthX / 2 * percent
        val minY = center.y - lengthY / 2 * percent
        val minZ = center.z - lengthZ / 2 * percent

        val maxX = center.x + lengthX / 2 * percent
        val maxY = center.y + lengthY / 2 * percent
        val maxZ = center.z + lengthZ / 2 * percent

        return Box(
            minX, minY, minZ,
            maxX, maxY, maxZ
        )
    }
}