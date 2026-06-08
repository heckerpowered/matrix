/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network.syncher

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityDataRegistry
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.syncher.EntityDataSerializer

object ModEntityDataSerializers {
    val double = EntityDataSerializer.forValueType(ByteBufCodecs.DOUBLE)

    private var initialized = false

    fun onInitialize() {
        if (initialized) {
            return
        }
        FabricEntityDataRegistry.register(Matrix.identifier("double"), double)
        initialized = true
    }
}
