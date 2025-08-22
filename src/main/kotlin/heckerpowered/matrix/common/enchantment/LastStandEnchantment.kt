/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LAST_STAND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object LastStandEnchantment {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        if (event.attacker!!.world.isClient) {
            return ActionResult.PASS
        }

        val attacker = event.attacker
        val lastStandEnchantmentEntry = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(LAST_STAND_ENCHANTMENT_KEY)
        val lastStandEnchantmentLevel = EnchantmentHelper.getEquipmentLevel(lastStandEnchantmentEntry, attacker)
        if (lastStandEnchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        if (attacker.health > attacker.maxHealth * 0.5) {
            return ActionResult.PASS
        }

        val minThreshold = attacker.maxHealth * 0.25
        val maxThreshold = attacker.maxHealth * 0.5
        val currentHealth = attacker.health.toDouble().coerceIn(minThreshold..maxThreshold)
        val ratio = 1 - currentHealth.inverseLerp(minThreshold..maxThreshold)

        val minBonus = 0.07 + (lastStandEnchantmentLevel - 1) * 0.04
        val maxBonus = 0.14 + (lastStandEnchantmentLevel - 1) * 0.04

        val damageBonusRatio = ratio.lerp(minBonus..maxBonus)
        event.damageMultiplier += damageBonusRatio

        return ActionResult.PASS
    }
}