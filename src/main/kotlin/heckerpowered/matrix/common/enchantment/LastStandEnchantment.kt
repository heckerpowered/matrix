/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LAST_STAND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys

object LastStandEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (attacker.world.isClient) {
            return
        }

        val lastStandEnchantmentEntry = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(LAST_STAND_ENCHANTMENT_KEY)
        val lastStandEnchantmentLevel = EnchantmentHelper.getEquipmentLevel(lastStandEnchantmentEntry, attacker)
        if (lastStandEnchantmentLevel <= 0) {
            return
        }

        if (attacker.health > attacker.maxHealth * 0.5) {
            return
        }

        val minThreshold = attacker.maxHealth * 0.25
        val maxThreshold = attacker.maxHealth * 0.5
        val currentHealth = attacker.health.toDouble().coerceIn(minThreshold..maxThreshold)
        val ratio = 1 - currentHealth.inverseLerp(minThreshold..maxThreshold)

        val minBonus = 0.07 + (lastStandEnchantmentLevel - 1) * 0.04
        val maxBonus = 0.14 + (lastStandEnchantmentLevel - 1) * 0.04

        val damageBonusRatio = ratio.lerp(minBonus..maxBonus)
        context.damageMultiplier += damageBonusRatio
    }
}
