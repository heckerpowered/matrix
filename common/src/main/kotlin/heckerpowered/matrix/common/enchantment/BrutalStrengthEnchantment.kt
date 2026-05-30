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
import heckerpowered.matrix.common.tag.MatrixDamageTypes

object BrutalStrengthEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        val target = context.target

        val enchantmentLevel = attacker.getEnchantmentLevel(ModEnchantments.brutalStrength)
        if (enchantmentLevel <= 0) return

        if (attacker.lastHurtMob != target &&
            context.source.`is`(MatrixDamageTypes.magic)
        ) {
            context.damageMultiplier += enchantmentLevel * 0.08
            attacker.setLastHurtMob(target)
        }
    }
}
