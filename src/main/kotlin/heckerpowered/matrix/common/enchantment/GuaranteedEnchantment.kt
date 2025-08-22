/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.GUARANTEED_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object GuaranteedEnchantment {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        if (event.attacker!!.world.isClient) {
            return ActionResult.PASS
        }

        val attacker = event.attacker
        val target = event.target
        val guaranteedEnchantmentEntry = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(GUARANTEED_ENCHANTMENT_KEY)
        val guaranteedEnchantmentLevel = EnchantmentHelper.getEquipmentLevel(guaranteedEnchantmentEntry, attacker)
        if (guaranteedEnchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        val percentage = target.maxHealth.toDouble() / attacker.maxHealth.toDouble()
        if (percentage <= 1) {
            return ActionResult.PASS
        }

        val damageBonusRatio = (percentage - 1).coerceAtMost(0.3) * guaranteedEnchantmentLevel
        event.damageMultiplier += damageBonusRatio
        return ActionResult.PASS
    }
}