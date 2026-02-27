/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.secondWind
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.RegistryKeys

object SecondWindEnchantment : DamageOutcomeRule {
    fun onInitialize() {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val target = context.target
        val secondWindEnchantmentEntry = target.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(secondWind)
        val level = EnchantmentHelper.getEquipmentLevel(secondWindEnchantmentEntry, target)
        if (level <= 0) {
            return
        }
        target.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, level * 20 * 5, 0))
    }
}
