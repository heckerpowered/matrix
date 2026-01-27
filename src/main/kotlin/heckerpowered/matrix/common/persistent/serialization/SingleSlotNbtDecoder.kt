/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder

abstract class SingleSlotNbtDecoder(codec: NbtCodec) : NbtDecoder(codec) {
    private var consumed = false
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (!consumed) {
            consumed = true; 0
        } else CompositeDecoder.DECODE_DONE
}