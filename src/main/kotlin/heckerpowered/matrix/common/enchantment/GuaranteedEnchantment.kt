/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.guaranteed
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys

object GuaranteedEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (attacker.world.isClient) {
            return
        }

        val target = context.target
        val guaranteedEnchantmentEntry = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(guaranteed)
        val guaranteedEnchantmentLevel = EnchantmentHelper.getEquipmentLevel(guaranteedEnchantmentEntry, attacker)
        if (guaranteedEnchantmentLevel <= 0) {
            return
        }

        val percentage = target.maxHealth.toDouble() / attacker.maxHealth.toDouble()
        if (percentage <= 1) {
            return
        }

        val damageBonusRatio = (percentage - 1).coerceAtMost(0.3) * guaranteedEnchantmentLevel
        context.damageMultiplier += damageBonusRatio
    }
}
