/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.combat.damage

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

data class DamageSettlementContext(
    override val target: LivingEntity,
    override val source: DamageSource,
    override val rawDamage: Float,
    val reducedDamage: Float,
    val realizedDamage: Float,
) : DamageContext {
    val retention: Float = if (rawDamage > 0f) realizedDamage / rawDamage else 0f

    var remainingDamage: Float = realizedDamage
        private set

    fun consume(capacity: Float): Float {
        if (capacity <= 0f || remainingDamage <= 0f) return 0f
        val consumed = capacity.coerceAtMost(remainingDamage)
        remainingDamage -= consumed
        return consumed
    }
}
