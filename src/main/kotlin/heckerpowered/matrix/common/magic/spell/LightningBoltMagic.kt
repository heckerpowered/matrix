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
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.common.balance.Accumulator

object LightningBoltMagic : Magic(
    MagicDefinition(
        Matrix.identifier("lightning_bolt"),
        15.mana,
        20.ticks
    )
) {
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

    override fun getCost(context: MagicCalculationContext): Long {
        val queue = context.queue
        val accumulator = Accumulator()
        if (queue != null) {
            val count = queue.channelingMagics().count { channelingMagic -> channelingMagic.magic is LightningBoltMagic }
            val costReduction = (count * 0.2).coerceAtMost(0.8)
            accumulator.pushCostReduction(costReduction)
        }

        return super.getCost(context)
    }
}