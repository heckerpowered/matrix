/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magic

data class ChannellingMagic(
    val magic: Magic,
    var currentChannelTime: Long,
    val channelTime: Long,
    val cost: Long,
    val data: MagicData,
)