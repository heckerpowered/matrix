/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extensions

import heckerpowered.matrix.core.MatrixMath.eulerToQuaternion
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import org.joml.Quaternionf

object EntityExtensions {
    val Entity.rotation: Quaternionf
        get() = eulerToQuaternion(yaw, pitch)

    fun Entity.damage(amount: Float, damageSource: DamageSource): Boolean {
        val previous = timeUntilRegen
        timeUntilRegen = 0
        val result = damage(damageSource, amount)
        timeUntilRegen = previous
        return result
    }
}