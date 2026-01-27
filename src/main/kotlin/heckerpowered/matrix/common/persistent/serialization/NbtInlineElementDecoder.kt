/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import net.minecraft.nbt.*

class NbtInlineElementDecoder(
    codec: NbtCodec,
    private val tagProvider: () -> NbtElement,
) : SingleSlotNbtDecoder(codec) {
    private inline fun <reified T : NbtElement> tag(): T = tagProvider() as T

    override fun decodeBoolean() = tag<NbtByte>().byteValue().toInt() != 0
    override fun decodeByte() = tag<NbtByte>().byteValue()
    override fun decodeShort() = tag<NbtShort>().shortValue()
    override fun decodeChar() = tag<NbtString>().asString().firstOrNull() ?: '\u0000'
    override fun decodeInt() = tag<NbtInt>().intValue()
    override fun decodeLong() = tag<NbtLong>().longValue()
    override fun decodeFloat() = tag<NbtFloat>().floatValue()
    override fun decodeDouble() = tag<NbtDouble>().doubleValue()
    override fun decodeString(): String = tag<NbtString>().asString()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> NbtObjectDecoder(codec, tag<NbtCompound>())
        StructureKind.MAP -> NbtMapDecoder(codec, tag<NbtCompound>())
        StructureKind.LIST -> NbtListDecoder(codec, tag<NbtList>())
        else -> error("Unsupported structure in inline value: ${descriptor.kind}")
    }
}