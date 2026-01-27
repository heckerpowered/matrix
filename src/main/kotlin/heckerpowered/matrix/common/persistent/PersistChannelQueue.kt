/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent

import heckerpowered.matrix.common.magic.ChannelingMagic
import heckerpowered.matrix.common.persistent.serialization.seralizer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PersistChannelQueue(
    @Serializable(with = UUIDSerializer::class)
    var channelerUuid: UUID,
    var isLocked: Boolean = false,
    var active: ChannelingMagic? = null,
    val queue: List<ChannelingMagic>,
)