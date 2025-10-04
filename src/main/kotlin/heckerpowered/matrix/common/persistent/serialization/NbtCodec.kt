/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.minecraft.nbt.NbtCompound

class NbtCodec(
    val serializersModule: SerializersModule = EmptySerializersModule(),
) {
    companion object {
        inline fun <reified T> encode(
            value: T,
            codec: NbtCodec = NbtCodec(),
            serializer: SerializationStrategy<T> = serializer(),
        ): NbtCompound {
            val encoder = RootNbtEncoder(codec)
            encoder.encodeSerializableValue(serializer, value)
            return encoder.result
        }

        inline fun <reified T> decode(
            tag: NbtCompound,
            codec: NbtCodec = NbtCodec(),
            deserializer: DeserializationStrategy<T> = serializer(),
        ): T {
            val decoder = RootNbtDecoder(codec, tag)
            return decoder.decodeSerializableValue(deserializer)
        }
    }
}