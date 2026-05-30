/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag

class NbtListDecoder(
    codec: NbtCodec,
    private val list: ListTag,
) : NbtContainerDecoder(codec) {

    private var cursor = 0

    override fun selectNextIndex(descriptor: SerialDescriptor): Int {
        if (cursor >= list.size) {
            return CompositeDecoder.DECODE_DONE
        }
        val index = cursor
        cursor += 1
        return index
    }

    override fun currentTag(): Tag {
        check(currentIndex in list.indices) { "Invalid list index: $currentIndex" }
        return list[currentIndex]
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        NbtInlineElementDecoder(codec) { list[index] }
}