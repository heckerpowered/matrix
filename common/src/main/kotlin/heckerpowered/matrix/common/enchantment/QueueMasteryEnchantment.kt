/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.enchantment.ModEnchantments.QueueMastery
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelQueueSizeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.persistent.queueSize
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.entity.player.Player

object QueueMasteryEnchantment : MagicCalculationContributor, CalculationContributor, ChannelEffect, DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<CalculationContributor>(this)
        RuleRegistry.register<ChannelEffect>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        // Queue Mastery: +15% damage against enemies with a locked queue.
        val lockedCount = context.target.channelQueues.values.count { it.isLocked }
        context.damageMultiplier += 0.15 * lockedCount
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is CostCalculationSink) return
        val caster = context.playerOrNull() ?: return
        if (caster.wizardHelmetStack.getEnchantmentLevel(caster.level(), QueueMastery) <= 0) return

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        val queue = context.queue
        val queuedMagicCount = queue?.queuedMagicCount
        val queueSizeOneOffFull = caster.queueSize - 1
        if (queuedMagicCount == queueSizeOneOffFull) {
            sink.costReduction += 0.5
        }
    }


    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        if (sink !is ChannelQueueSizeCalculationSink) return
        val caster = context.playerOrNull() ?: return
        if (caster.getEnchantmentLevel(QueueMastery) <= 0) return

        sink.queueSize += 1
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.entityOrNull() as? Player ?: return
        if (caster.getEnchantmentLevel(QueueMastery) <= 0) return

        // Queue Mastery: The last magic to fill a queue has -50% mana cost and
        // locks the queue until all magics have channeled.
        val queue = invocation.queue
        val queuedMagicCount = queue.queuedMagicCount
        if (queuedMagicCount == caster.queueSize) {
            // Queue is automatically unlocked when queue is empty
            queue.isLocked = true
        }
    }
}
