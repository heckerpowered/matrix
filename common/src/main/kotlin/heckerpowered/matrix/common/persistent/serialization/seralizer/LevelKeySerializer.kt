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
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

object LevelKeySerializer : KSerializer<ResourceKey<Level>> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("WorldKey") {
            element<String>("namespace")
            element<String>("path")
        }

    override fun serialize(encoder: Encoder, value: ResourceKey<Level>) {
        val identifier = value.identifier()
        
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, identifier.namespace)
            encodeStringElement(descriptor, 1, identifier.path)
        }
    }

    override fun deserialize(decoder: Decoder): ResourceKey<Level> {
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

        val id = Identifier.fromNamespaceAndPath(namespace, path)
        return ResourceKey.create(Registries.DIMENSION, id)
    }
}