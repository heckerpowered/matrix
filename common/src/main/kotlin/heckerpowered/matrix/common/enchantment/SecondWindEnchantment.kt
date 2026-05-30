/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

object SecondWindEnchantment : DamageOutcomeRule {
    fun onInitialize() {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val target = context.target

        val enchantmentLevel = target.getEnchantmentLevel(ModEnchantments.secondWind).takeIf { it > 0 } ?: return
        target.addEffect(MobEffectInstance(MobEffects.REGENERATION, enchantmentLevel * 20 * 5, 0))
    }
}
