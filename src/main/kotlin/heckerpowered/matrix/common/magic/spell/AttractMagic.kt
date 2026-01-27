/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.entity.AttractorEntity
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object AttractMagic : Magic(
    MagicDefinition(
        Matrix.identifier("attract"),
        20.mana,
        20.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        target.world.spawnEntity(AttractorEntity(target.world).also {
            it.setPosition(target.pos)
            it.owner = player
        })
    }
}