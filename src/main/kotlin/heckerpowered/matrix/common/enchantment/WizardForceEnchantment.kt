/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.wizardForce
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity

object WizardForceEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (attacker !is ServerPlayerEntity) {
            return
        }
        if (!context.source.isOf(MatrixDamageTypes.magic)) {
            return
        }

        val level = attacker.wizardHelmet.getEnchantmentLevel(wizardForce)
        context.damageMultiplier += level * 0.05
    }
}
