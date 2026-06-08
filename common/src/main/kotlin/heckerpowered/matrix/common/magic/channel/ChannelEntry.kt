/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.magic.core.Magic
import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ChannelEntry(
    @Contextual val magic: Magic,
    val cost: Long,
    val channelTime: Long,
    var currentChannelTime: Long = 0,
    @Polymorphic val payload: ExecutionPayload = ExecutionPayload(),
) {
    @Transient
    var clientPrediction: Boolean = false
}
