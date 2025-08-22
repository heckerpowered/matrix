/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.util.ActionResult

object ExposedEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xFF0000
) {
    init {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        val statusEffect = event.target.getStatusEffect(EXPOSED_EFFECT) ?: return ActionResult.PASS
        event.damageMultiplier += (statusEffect.amplifier + 1) * 0.4
        return ActionResult.PASS
    }
}