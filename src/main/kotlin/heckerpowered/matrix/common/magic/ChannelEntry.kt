/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic
import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class ChannelEntry(
    @Contextual val magic: Magic,
    val cost: Long,
    val channelTime: Long,
    var currentChannelTime: Long = 0,
    @Polymorphic val data: ExecutionPayload = ExecutionPayload(),
)