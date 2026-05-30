/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.common.magic.channel.ChannelQueue
import net.minecraft.world.entity.LivingEntity
import java.util.*

data class QueueLoadedContext(
    val entity: LivingEntity,
    val uuid: UUID,
    val queue: ChannelQueue,
)
