/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
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