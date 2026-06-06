/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.ModEnchantments.MagicShield
import heckerpowered.matrix.common.entity.rule.AttributeComputationContext
import heckerpowered.matrix.common.entity.rule.AttributeComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.minecraft.world.entity.player.Player

object MagicShieldEnchantment : AttributeComputationRule {
    init {
        RuleRegistry.register<AttributeComputationRule>(this)
    }

    fun onInitialize() {
    }

    override fun onComputation(context: AttributeComputationContext) {
        val entity = context.entity as? Player ?: return
        val magicShieldLevel = entity.getEnchantmentLevel(MagicShield)
        if (magicShieldLevel <= 0) return

        val mana = entity.mana.toDouble()
        val maxMana = entity.maxMana.toDouble()
        val percentage = mana.inverseLerp((maxMana * 0.5)..maxMana).coerceIn(.0..1.0)
        if (percentage.isNaN() || percentage.isInfinite()) return

        context.multiplier += percentage * 0.2 * magicShieldLevel
    }
}