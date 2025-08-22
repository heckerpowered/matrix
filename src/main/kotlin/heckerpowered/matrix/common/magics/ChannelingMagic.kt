/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic

data class ChannelingMagic(
    val magic: Magic,
    var currentChannelTime: Long,
    val channelTime: Long,
    val cost: Long,
    val data: MagicData,
)