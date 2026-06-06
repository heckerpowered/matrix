/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory

object ExposedEffect : MobEffect(
    MobEffectCategory.HARMFUL,
    0xFF0000
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        val statusEffect = target.getEffect(ModMobEffects.Exposed) ?: return
        context.damageMultiplier += (statusEffect.amplifier + 1) * 0.4
    }
}