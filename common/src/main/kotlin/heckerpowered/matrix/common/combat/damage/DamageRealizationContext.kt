/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

data class DamageRealizationContext(
    override val target: LivingEntity,
    override val source: DamageSource,
    override val rawDamage: Float,
    val reducedDamage: Float,
) : DamageContext {
    val baseRetention: Float = if (rawDamage > 0f) reducedDamage / rawDamage else 0f
    var retention: Float = baseRetention
    val realizedDamage: Float
        get() = rawDamage * retention
}