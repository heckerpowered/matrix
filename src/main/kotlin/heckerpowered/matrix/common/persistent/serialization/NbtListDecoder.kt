/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList

class NbtListDecoder(
    codec: NbtCodec,
    private val list: NbtList,
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

    override fun currentTag(): NbtElement {
        check(currentIndex in list.indices) { "Invalid list index: $currentIndex" }
        return list[currentIndex]
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        NbtInlineElementDecoder(codec) { list[index] }
}