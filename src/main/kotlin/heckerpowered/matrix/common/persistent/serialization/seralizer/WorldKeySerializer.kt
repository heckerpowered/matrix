/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization.seralizer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World

object WorldKeySerializer : KSerializer<RegistryKey<World>> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("WorldKey") {
            element<String>("namespace")
            element<String>("path")
        }

    override fun serialize(encoder: Encoder, value: RegistryKey<World>) {
        val id = value.value

        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, id.namespace)
            encodeStringElement(descriptor, 1, id.path)
        }
    }

    override fun deserialize(decoder: Decoder): RegistryKey<World> {
        var namespace: String? = null
        var path: String? = null

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> namespace = decodeStringElement(descriptor, 0)
                    1 -> path = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
        }

        require(!namespace.isNullOrBlank()) { "Missing world namespace" }
        require(!path.isNullOrBlank()) { "Missing world path" }

        val id = Identifier.of(namespace, path)
        return RegistryKey.of(RegistryKeys.WORLD, id)
    }
}