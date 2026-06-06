/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.enchantment.ModEnchantments.WizardForce
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.level.ServerPlayer

object WizardForceEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? ServerPlayer ?: return
        if (!context.source.`is`(MatrixDamageTypes.magic)) return

        val level = attacker.getEnchantmentLevel(WizardForce).takeIf { it > 0 } ?: return
        context.damageMultiplier += level * 0.05
    }
}
