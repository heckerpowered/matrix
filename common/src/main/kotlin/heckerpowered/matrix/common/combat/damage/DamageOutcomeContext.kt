/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.LivingEntity

data class DamageOutcomeContext(
    override val target: LivingEntity,
    override val source: DamageSource,
    override val rawDamage: Float,
    val reducedDamage: Float,
    val retention: Float,
) : DamageContext {
    val realizedDamage: Float = rawDamage * retention
    val baseRetention: Float = if (rawDamage > 0f) reducedDamage / rawDamage else 0f
}