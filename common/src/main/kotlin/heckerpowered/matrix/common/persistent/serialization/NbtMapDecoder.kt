/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

class NbtMapDecoder(
    codec: NbtCodec,
    private val compound: CompoundTag,
) : NbtContainerDecoder(codec) {

    private val namedKeys: List<String> = compound.keySet().toList()
    private val limit: Int = namedKeys.size * 2  // key,value,key,value,...

    private var flat = 0
    private var keySlot = true
    private var currentKeyText: String? = null

    override fun selectNextIndex(descriptor: SerialDescriptor): Int {
        if (flat >= limit) return CompositeDecoder.DECODE_DONE

        val index = flat
        flat += 1
        keySlot = index % 2 == 0
        if (keySlot) {
            currentKeyText = namedKeys[index / 2]
        }
        return index
    }

    override fun currentTag(): Tag {
        check(!keySlot) { "Map key slot has no NbtElement" }
        val key = currentKeyText ?: error("Map value requested before key")
        return compound.get(key) ?: error("Missing map value for key '$key'")
    }

    override fun decodeString(): String =
        if (keySlot) currentKeyText!! else super.decodeString()

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val name = decodeString()
        val index = enumDescriptor.getElementIndex(name)
        if (index == CompositeDecoder.UNKNOWN_NAME) {
            error("Unknown enum in map: '$name' for ${enumDescriptor.serialName}")
        }
        return index
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        check(!keySlot) { "Map key cannot be inline/structured: ${descriptor.serialName}" }
        return NbtInlineElementDecoder(codec) { currentTag() }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        check(!keySlot) { "Map key cannot be a structure (${descriptor.serialName})" }
        return super.beginStructure(descriptor)
    }
}