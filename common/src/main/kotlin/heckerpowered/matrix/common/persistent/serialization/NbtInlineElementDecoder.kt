/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import net.minecraft.nbt.*
import kotlin.jvm.optionals.getOrNull

class NbtInlineElementDecoder(
    codec: NbtCodec,
    private val tagProvider: () -> Tag,
) : SingleSlotNbtDecoder(codec) {
    private inline fun <reified T : Tag> tag(): T = tagProvider() as T

    override fun decodeBoolean() = tag<ByteTag>().byteValue().toInt() != 0
    override fun decodeByte() = tag<ByteTag>().byteValue()
    override fun decodeShort() = tag<ShortTag>().shortValue()
    override fun decodeChar() = tag<StringTag>().asString().getOrNull()?.firstOrNull() ?: '\u0000'
    override fun decodeInt() = tag<IntTag>().intValue()
    override fun decodeLong() = tag<LongTag>().longValue()
    override fun decodeFloat() = tag<FloatTag>().floatValue()
    override fun decodeDouble() = tag<DoubleTag>().doubleValue()
    override fun decodeString(): String = tag<StringTag>().asString().getOrNull() ?: ""

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> NbtObjectDecoder(codec, tag<CompoundTag>())
        StructureKind.MAP -> NbtMapDecoder(codec, tag<CompoundTag>())
        StructureKind.LIST -> NbtListDecoder(codec, tag<ListTag>())
        else -> error("Unsupported structure in inline value: ${descriptor.kind}")
    }
}