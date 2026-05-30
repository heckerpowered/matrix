/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

object ExposedEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xFF0000
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        val statusEffect = target.getStatusEffect(EXPOSED_EFFECT) ?: return
        context.damageMultiplier += (statusEffect.amplifier + 1) * 0.4
    }
}