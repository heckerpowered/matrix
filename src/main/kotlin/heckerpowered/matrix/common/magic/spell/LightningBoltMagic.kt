/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.entity.MagicLightningEntity
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic.Companion.pushCostReduction
import heckerpowered.matrix.core.common.balance.Accumulator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object LightningBoltMagic : Magic(
    MagicDefinition(
        Matrix.identifier("lightning_bolt"),
        15.mana,
        20.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
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
            if (!data.isSpoofed) {
                it.channeler = player
            }
        })
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: ExecutionPayload, accumulator: Accumulator): Long {
        if (sequence != null) {
            val count = sequence.channelingMagics().count { channelingMagic -> channelingMagic.magic is LightningBoltMagic }
            val costReduction = (count * 0.2).coerceAtMost(0.8)
            accumulator.pushCostReduction(costReduction)
        }

        return super.getCost(player, target, sequence, data, accumulator)
    }
}