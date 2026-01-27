/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extensions

import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.random.Random

object BoxExtensions {
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