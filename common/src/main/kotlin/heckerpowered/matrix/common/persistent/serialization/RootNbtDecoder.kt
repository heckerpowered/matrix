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
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class RootNbtDecoder(
    codec: NbtCodec,
    private val root: CompoundTag,
) : NbtDecoder(codec) {

    companion object {
        private const val ROOT = "value"
    }

    private var consumed = false

    override fun decodeBoolean(): Boolean = root.getBooleanOr(ROOT, false)
    override fun decodeByte(): Byte = root.getByteOr(ROOT, 0)
    override fun decodeShort(): Short = root.getShortOr(ROOT, 0)
    override fun decodeChar(): Char = root.getStringOr(ROOT, "").firstOrNull() ?: '\u0000'
    override fun decodeInt(): Int = root.getIntOr(ROOT, 0)
    override fun decodeLong(): Long = root.getLongOr(ROOT, 0)
    override fun decodeFloat(): Float = root.getFloatOr(ROOT, 0F)
    override fun decodeDouble(): Double = root.getDoubleOr(ROOT, .0)
    override fun decodeString(): String = root.getStringOr(ROOT, "")

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT,
        PolymorphicKind.OPEN, PolymorphicKind.SEALED,
            ->
            NbtObjectDecoder(codec, root)

        StructureKind.MAP ->
            NbtMapDecoder(codec, root)

        StructureKind.LIST -> {
            val list = root.get(ROOT) as ListTag
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