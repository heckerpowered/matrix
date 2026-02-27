/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MANA_OVERFLOW_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getOrCreateChannelQueue
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MaxManaCalculationSink
import heckerpowered.matrix.common.magic.rule.effect.CastEffect
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import kotlin.random.Random

object ManaOverflowEnchantment : CalculationContributor, CastEffect {
    fun onInitialize() {
        RuleRegistry.register<CalculationContributor>(this)
        RuleRegistry.register<CastEffect>(this)
    }

    override fun onCast(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.asPlayerOrNull() ?: return
        val target = invocation.target
        val payload = invocation.payload

        if (!caster.isBloodPactActive) return
        if (caster.wizardHelmet.getEnchantmentLevel(MANA_OVERFLOW_ENCHANTMENT_KEY) < 5) return
        if (payload.isSpread) return
        if (Random.nextBoolean()) return

        val nearestEntity = target.getNearestEntities(20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != caster && it.isAlive }
            .firstOrNull { caster.getChannelQueue(it)?.isEmpty ?: true }
            ?: return

        val spreadInvocation = MagicInvocation(
            caster = invocation.caster,
            target = nearestEntity,
            queue = nearestEntity.getOrCreateChannelQueue(caster),
            payload = ExecutionPayload(isSpread = true)
        )

        ChannelExecutor.channel(magic, spreadInvocation)
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        if (sink !is MaxManaCalculationSink) return
        val caster = context.playerOrNull() ?: return
        val manaOverflowLevel = caster.wizardHelmet.getEnchantmentLevel(MANA_OVERFLOW_ENCHANTMENT_KEY)
        if (manaOverflowLevel <= 0) return

        sink.multiplier += manaOverflowLevel * 0.2
    }
}