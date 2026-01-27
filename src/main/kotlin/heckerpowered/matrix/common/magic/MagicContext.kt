/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.magic.channel.ChannelQueue
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity

data class MagicContext(
    val player: PlayerEntity,
    val target: LivingEntity,
    val channelQueue: ChannelQueue?,
)
