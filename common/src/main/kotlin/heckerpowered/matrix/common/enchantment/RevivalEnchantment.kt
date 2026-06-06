/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.entity.rule.LivingHealContext
import heckerpowered.matrix.common.entity.rule.LivingHealRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register

object RevivalEnchantment : LivingHealRule {
    init {
        RuleRegistry.register<LivingHealRule>(this)
    }

    fun onInitialize() {
    }

    override fun onHeal(context: LivingHealContext) {
        if (context.healAmount <= 0) return

        val entity = context.entity
        val enchantmentLevel = entity.getEnchantmentLevel(ModEnchantments.Revival).takeIf { it > 0 } ?: return

        if (entity.health < entity.maxHealth * 0.5) {
            context.multiplier += enchantmentLevel * 0.2F
        } else {
            context.multiplier += enchantmentLevel * 0.2F * 2
        }
    }
}