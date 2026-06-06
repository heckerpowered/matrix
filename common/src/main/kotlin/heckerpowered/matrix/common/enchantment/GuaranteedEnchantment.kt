/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register

object GuaranteedEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (attacker.level().isClientSide) return

        val target = context.target
        val guaranteedEnchantmentLevel = attacker.getEnchantmentLevel(ModEnchantments.Guaranteed)
        if (guaranteedEnchantmentLevel <= 0) return

        val percentage = target.maxHealth.toDouble() / attacker.maxHealth.toDouble()
        if (percentage <= 1) return

        val damageBonusRatio = (percentage - 1).coerceAtMost(0.3) * guaranteedEnchantmentLevel
        context.damageMultiplier += damageBonusRatio
    }
}
