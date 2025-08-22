/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.effect.bloodPactActive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object PeakOverdriveEnchantment {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        if (attacker !is PlayerEntity || !attacker.bloodPactActive) {
            return ActionResult.PASS
        }

        val equippedHelmet = attacker.wizardHelmet
        if (equippedHelmet.isEmpty) {
            return ActionResult.PASS
        }

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(PEAK_OVERDRIVE_ENCHANTMENT_KEY)
        val enchantmentLevel = EnchantmentHelper.getLevel(enchantmentEntry, equippedHelmet)
        if (enchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        accumulator.damageMultiplier += 0.5
        return ActionResult.PASS
    }
}