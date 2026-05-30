/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.entity.MagicLightningBolt
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.asPlayerOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register

object LightningBoltMagic : Magic(
    MagicDefinition(
        Matrix.identifier("lightning_bolt"),
        15.mana,
        20.ticks
    )
), MagicCalculationContributor {
    init {
        RuleRegistry.register<MagicCalculationContributor>(this)
    }

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.asPlayerOrNull()
        val target = invocation.target
        val payload = invocation.payload

        val lightningTypes = MagicLightningBolt.LightningType.entries
        val lightningType = if ((0..1000).random() < 6) {
            MagicLightningBolt.LightningType.BLACK
        } else {
            var lightningType = lightningTypes.random()
            while (lightningType == MagicLightningBolt.LightningType.BLACK) {
                lightningType = lightningTypes.random()
            }
            lightningType
        }

        target.level().addFreshEntity(MagicLightningBolt(target.level()).also {
            it.setPos(target.position())
            it.lightningType = lightningType
            if (!payload.isSpoofed) {
                // it. = caster // TODO:
            }
        })
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is CostCalculationSink) return
        if (magic !is LightningBoltMagic) return
        val queue = context.queue ?: return

        val count = queue.channelingMagics().count { it.magic is LightningBoltMagic }
        val costReduction = (count * 0.2).coerceAtMost(0.8)
        sink.costReduction += costReduction
    }
}