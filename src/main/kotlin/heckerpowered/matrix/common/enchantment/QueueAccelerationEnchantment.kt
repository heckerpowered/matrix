/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.queueAcceleration
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.*
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register

object QueueAccelerationEnchantment : MagicCalculationContributor, CalculationContributor {
    fun onInitialize() {
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<CalculationContributor>(this)
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is ChannelTimeCalculationSink) return
        val player = context.playerOrNull() ?: return
        val queue = context.queue ?: return
        if (player.wizardHelmet.getEnchantmentLevel(queueAcceleration) <= 0) return

        // Queue Acceleration: +60% channel speed for magics third or later in the queue.
        if (queue.channelingMagicCount >= 2) {
            sink.channelSpeedBonus += 0.6
        }
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        val caster = context.playerOrNull() ?: return
        if (caster.wizardHelmet.getEnchantmentLevel(queueAcceleration) <= 0) return

        when (sink) {
            is ChannelQueueSizeCalculationSink -> sink.queueSize += 1
            is MaxManaCalculationSink -> sink.maxMana += 1
        }
    }
}