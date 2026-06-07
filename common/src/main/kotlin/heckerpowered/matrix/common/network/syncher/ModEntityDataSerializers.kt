/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network.syncher

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers

object ModEntityDataSerializers {
    val double = EntityDataSerializer.forValueType(ByteBufCodecs.DOUBLE)

    private var initialized = false

    fun onInitialize() {
        if (initialized) {
            return
        }
        EntityDataSerializers.registerSerializer(double)
        initialized = true
    }
}
