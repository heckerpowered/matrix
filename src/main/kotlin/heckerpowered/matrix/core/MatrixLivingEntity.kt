/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.common.entity.EntityProtection
import heckerpowered.matrix.common.entity.EntityProtection.Companion.protection
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import net.minecraft.entity.LivingEntity
import java.util.*

interface MatrixLivingEntity {
    @SuppressWarnings("all")
    var `matrix$killed`: Boolean

    fun getChannelQueues(): MutableMap<UUID, ChannelQueue>
}

/**
 * Matrix kill marker for a [LivingEntity].
 *
 * Behavior and characteristics:
 * - When `true`, [EntityProtection.protection] resolves to [EntityProtection.DEAD].
 * - DEAD protection only forces health reads/writes to `0` via `getHealth`/`setHealth`.
 */
var LivingEntity.killed: Boolean
    get() = (this as? MatrixLivingEntity)?.`matrix$killed` ?: false
    set(value) {
        (this as? MatrixLivingEntity)?.`matrix$killed` = value
    }
