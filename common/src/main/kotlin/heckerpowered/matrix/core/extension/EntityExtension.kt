/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core.extension

import heckerpowered.matrix.core.eulerToQuaternion
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import org.joml.Quaternionf

val Entity.rotation: Quaternionf
    get() = eulerToQuaternion(xRot, yRot)

fun Entity.damage(amount: Float, damageSource: DamageSource): Boolean {
    val level = level()
    if (level !is ServerLevel) return false

    val previous = invulnerableTime
    invulnerableTime = 0
    val result = hurtServer(level, damageSource, amount)
    invulnerableTime = previous
    return result
}

fun Entity.damage(damageSource: DamageSource, amount: Float): Boolean {
    return damage(amount, damageSource)
}
