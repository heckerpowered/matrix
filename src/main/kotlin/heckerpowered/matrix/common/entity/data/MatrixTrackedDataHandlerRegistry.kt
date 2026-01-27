/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.data

import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.network.codec.PacketCodecs

object MatrixTrackedDataHandlerRegistry {
    val DOUBLE: TrackedDataHandler<Double> = TrackedDataHandler.create(PacketCodecs.DOUBLE)

    init {
        TrackedDataHandlerRegistry.register(DOUBLE)
    }
}