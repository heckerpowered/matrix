/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

class RootNbtDecoder(
    codec: NbtCodec,
    private val root: NbtCompound,
) : NbtDecoder(codec) {

    companion object {
        private const val ROOT = "value"
    }

    private var consumed = false

    override fun decodeBoolean(): Boolean = root.getBoolean(ROOT)
    override fun decodeByte(): Byte = root.getByte(ROOT)
    override fun decodeShort(): Short = root.getShort(ROOT)
    override fun decodeChar(): Char = root.getString(ROOT).firstOrNull() ?: '\u0000'
    override fun decodeInt(): Int = root.getInt(ROOT)
    override fun decodeLong(): Long = root.getLong(ROOT)
    override fun decodeFloat(): Float = root.getFloat(ROOT)
    override fun decodeDouble(): Double = root.getDouble(ROOT)
    override fun decodeString(): String = root.getString(ROOT)

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT,
        PolymorphicKind.OPEN, PolymorphicKind.SEALED,
            ->
            NbtObjectDecoder(codec, root)

        StructureKind.MAP ->
            NbtMapDecoder(codec, root)

        StructureKind.LIST -> {
            val list = root.get(ROOT) as NbtList
            NbtListDecoder(codec, list)
        }

        else -> this
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (!consumed) {
            consumed = true
            0
        } else {
            CompositeDecoder.DECODE_DONE
        }
    }
}