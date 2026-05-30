/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.extension

import heckerpowered.matrix.common.magic.channel.ChannelQueue
import java.util.*

interface MatrixLivingEntity {
    var polarity: Long
    var healthSpoofValue: Float
    val channelQueues: MutableMap<UUID, ChannelQueue>
}