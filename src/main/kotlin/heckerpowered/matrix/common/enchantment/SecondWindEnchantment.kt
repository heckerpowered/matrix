/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.SECOND_WIND_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object SecondWindEnchantment {
    fun onInitialize() {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        val target = event.target
        val secondWindEnchantmentEntry = target.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(SECOND_WIND_ENCHANTMENT_KEY)
        val level = EnchantmentHelper.getEquipmentLevel(secondWindEnchantmentEntry, target)
        if (level <= 0) {
            return ActionResult.PASS
        }
        target.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, level * 20 * 5, 0))

        return ActionResult.PASS
    }
}