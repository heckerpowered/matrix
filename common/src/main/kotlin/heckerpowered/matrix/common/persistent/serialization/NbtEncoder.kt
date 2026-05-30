/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

abstract class NbtEncoder(protected val codec: NbtCodec) : Encoder, CompositeEncoder {
    override val serializersModule: SerializersModule get() = codec.serializersModule

    @ExperimentalSerializationApi
    override fun encodeNull() {
    }

    @ExperimentalSerializationApi
    override fun encodeNotNullMark() {
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }
}