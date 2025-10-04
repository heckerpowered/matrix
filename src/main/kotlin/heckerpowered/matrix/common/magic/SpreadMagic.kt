/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.persistent.ChannelQueue
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object SpreadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("spread"),
        9.mana,
        9.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        if (player == null) {
            return
        }
        val magics = sequence.magics.filterIndexed { index, channelingMagic ->
            index > sequence.index && channelingMagic.magic != this
        }.map {
            it.magic
        }
        target.world.getOtherEntities(player, target.boundingBox.expand(24.0)).forEach {
            if (it !is LivingEntity || it == target) {
                return@forEach
            }
            for (magic in magics) {
                ChannelQueue.channelMagic(magic, player, it)
            }
        }
    }
}