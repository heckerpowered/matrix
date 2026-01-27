/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.common.persistent.serialization.seralizer.MagicReferenceSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
@SerialName("MagicData")
open class ExecutionPayload(
    var isSpread: Boolean = false,
    var isSpoofed: Boolean = false,
) {
    companion object {
        var serializationModule = SerializersModule {
            contextual(Magic::class, MagicReferenceSerializer)
            polymorphic(ExecutionPayload::class) {
                subclass(ExecutionPayload::class, serializer())
            }
        }
    }

    fun copyFrom(source: ExecutionPayload) {
        this.isSpread = source.isSpread
        this.isSpoofed = source.isSpoofed
    }
}