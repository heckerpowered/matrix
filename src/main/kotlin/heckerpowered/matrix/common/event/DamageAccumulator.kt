/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.event

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

data class DamageAccumulator(
    val attacker: LivingEntity?,
    val target: LivingEntity,
    var damageSource: DamageSource,
    val baseDamage: Double,
    var baseDamageBonus: Double = 0.0,
    var damageMultiplier: Double = 1.0,
    var damageReductionMultiplier: Double = 1.0,
    var immune: Boolean = false,
) {
    fun accumulateDamage(): Double {
        if (immune) {
            return .0
        }

        val damage = (baseDamage + baseDamageBonus) * damageMultiplier * damageReductionMultiplier
        return damage.coerceAtLeast(.0)
    }
}