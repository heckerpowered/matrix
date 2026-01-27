/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.common.magic.ChannelQueue
import net.minecraft.entity.LivingEntity
import java.util.*

interface MatrixLivingEntity {
    @SuppressWarnings("all")
    var `matrix$killed`: Boolean

    fun getChannelQueues(): MutableMap<UUID, ChannelQueue>
}

var LivingEntity.killed: Boolean
    get() = (this as? MatrixLivingEntity)?.`matrix$killed` ?: false
    set(value) {
        (this as? MatrixLivingEntity)?.`matrix$killed` = value
    }