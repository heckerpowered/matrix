/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_QUEUE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.*
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register

object MagicQueueEnchantment : MagicCalculationContributor, CalculationContributor {
    fun onInitialize() {
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<CalculationContributor>(this)
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is ChannelTimeCalculationSink) return
        val player = context.playerOrNull() ?: return
        val queue = context.queue ?: return
        if (player.wizardHelmet.getEnchantmentLevel(MAGIC_QUEUE_ENCHANTMENT_KEY) <= 0) return

        // Magic Queue: +30% channel speed for the second magic in a queue.
        if (queue.channelingMagicCount == 1) {
            sink.channelSpeedBonus += 0.3
        }
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        val caster = context.playerOrNull() ?: return
        if (caster.wizardHelmet.getEnchantmentLevel(MAGIC_QUEUE_ENCHANTMENT_KEY) <= 0) return

        when (sink) {
            is ChannelQueueSizeCalculationSink -> sink.queueSize += 1
            is MaxManaCalculationSink -> sink.maxMana += 1
        }
    }
}