/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.common.persistent.ChannelSequence
import net.minecraft.entity.LivingEntity
import java.util.*

interface MatrixLivingEntity {
    @SuppressWarnings("all")
    var `matrix$killed`: Boolean

    fun getChannelSequence(): MutableMap<UUID, ChannelSequence>
}

var LivingEntity.killed: Boolean
    get() = (this as? MatrixLivingEntity)?.`matrix$killed` ?: false
    set(value) {
        (this as? MatrixLivingEntity)?.`matrix$killed` = value
    }