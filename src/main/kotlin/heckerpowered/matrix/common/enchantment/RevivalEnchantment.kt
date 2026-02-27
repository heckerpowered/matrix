/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.revival
import heckerpowered.matrix.common.event.LivingHealCallback
import heckerpowered.matrix.common.event.LivingHealEvent
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object RevivalEnchantment {
    fun onInitialize() {
        LivingHealCallback.EVENT.register(::onLivingHeal)
    }

    private fun onLivingHeal(event: LivingHealEvent): ActionResult {
        if (event.amount <= 0) {
            return ActionResult.PASS
        }

        val entity = event.entity
        val revivalEnchantmentEntry = entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(revival)
        val level = EnchantmentHelper.getEquipmentLevel(revivalEnchantmentEntry, entity)
        if (level <= 0) {
            return ActionResult.PASS
        }

        if (entity.health < entity.maxHealth * 0.5) {
            event.amount += event.amount * level * 0.2F
        } else {
            event.amount += event.amount * level * 0.2F * 2
        }
        return ActionResult.PASS
    }
}