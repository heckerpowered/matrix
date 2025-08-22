/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.BLOOD_PACT_EFFECT
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

val PlayerEntity.bloodPactActive: Boolean
    get() = hasStatusEffect(BLOOD_PACT_EFFECT)

object BloodPactEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF0000
) {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        if (attacker.hasStatusEffect(BLOOD_PACT_EFFECT) &&
            accumulator.damageSource.isOf(MatrixDamageTypes.magic)
        ) {
            accumulator.damageMultiplier += 0.1
        }

        return ActionResult.PASS
    }
}