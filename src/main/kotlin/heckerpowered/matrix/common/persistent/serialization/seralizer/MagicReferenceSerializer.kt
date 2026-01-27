/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization.seralizer

import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.system.MagicManager
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

object MagicReferenceSerializer : KSerializer<Magic> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("matrix.magic_reference", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Magic) {
        encoder.encodeString(value.definition.uuid.toString())
    }

    override fun deserialize(decoder: Decoder): Magic {
        val raw = decoder.decodeString()
        val uuid = try {
            UUID.fromString(raw)
        } catch (exception: IllegalArgumentException) {
            throw SerializationException("Invalid Magic UUID: '$raw'", exception)
        }
        return MagicManager.getMagicByUuid(uuid)
            ?: throw SerializationException("Unknown Magic UUID: $uuid (not registered)")
    }
}