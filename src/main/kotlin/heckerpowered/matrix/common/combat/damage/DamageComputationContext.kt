/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

data class DamageComputationContext(
    override val target: LivingEntity,
    override val source: DamageSource,
    override val rawDamage: Float,
) : CancellableDamageContext {

    var baseDamageBonus: Double = 0.0
    var damageMultiplier: Double = 1.0
    var damageReductionMultiplier: Double = 1.0

    private var cancelled: Boolean = false

    override val isCancelled get() = cancelled

    override fun cancel() {
        cancelled = true
    }

    fun computeDamage(): Float {
        val damage = (rawDamage + baseDamageBonus) * damageMultiplier * damageReductionMultiplier
        return damage.coerceAtLeast(.0).toFloat()
    }
}