/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.brutalStrength
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.registry.RegistryKeys

object BrutalStrengthEnchantment : DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        val target = context.target

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(brutalStrength)
        val equippedHelmet = attacker.getEquippedStack(EquipmentSlot.HEAD)
        val enchantmentLevel = EnchantmentHelper.getLevel(enchantmentEntry, equippedHelmet)
        if (enchantmentLevel <= 0) {
            return
        }

        if (attacker.attacking != target &&
            context.source.isOf(MatrixDamageTypes.magic)
        ) {
            context.damageMultiplier += enchantmentLevel * 0.08
            attacker.onAttacking(target)
        }
    }
}
