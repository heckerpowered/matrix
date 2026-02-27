/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.entity.MagicLightningEntity
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

        val lightningTypes = MagicLightningEntity.LightningType.entries
        val lightningType = if ((0..1000).random() < 6) {
            MagicLightningEntity.LightningType.BLACK
        } else {
            var lightningType = lightningTypes.random()
            while (lightningType == MagicLightningEntity.LightningType.BLACK) {
                lightningType = lightningTypes.random()
            }
            lightningType
        }

        target.world.spawnEntity(MagicLightningEntity(target.world).also {
            it.setPosition(target.pos)
            it.lightningType = lightningType
            if (!payload.isSpoofed) {
                it.channeler = caster
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