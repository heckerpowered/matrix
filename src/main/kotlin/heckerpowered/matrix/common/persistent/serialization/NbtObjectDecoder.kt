/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement

class NbtObjectDecoder(
    codec: NbtCodec,
    private val compound: NbtCompound,
) : NbtContainerDecoder(codec) {

    private var fieldCursor = 0
    private var currentName: String? = null

    override fun selectNextIndex(descriptor: SerialDescriptor): Int {
        while (fieldCursor < descriptor.elementsCount) {
            val i = fieldCursor++
            val name = descriptor.getElementName(i)
            if (compound.contains(name)) {
                currentName = name
                return i
            }
        }
        currentName = null
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentTag(): NbtElement {
        val name = currentName ?: error("No current field selected")
        return compound.get(name) ?: error("Missing field '$name'")
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        NbtInlineElementDecoder(codec) { compound.get(descriptor.getElementName(index))!! }
}