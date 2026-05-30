/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.ManaRegenerationCalculationSink
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.entity.player.Player

object ManaRegenerationEnchantment : CalculationContributor {
    init {
        RuleRegistry.register<CalculationContributor>(this)
    }

    fun onInitialize() {
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        if (sink !is ManaRegenerationCalculationSink) return
        val caster = context.caster?.entityOrNull() as? Player ?: return
        val enchantmentLevel = caster.getEnchantmentLevel(ModEnchantments.manaRegeneration)
        if (enchantmentLevel <= 0) return

        sink.multiplier += enchantmentLevel * 0.3
    }
}