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
import net.minecraft.nbt.*
import kotlin.jvm.optionals.getOrNull

abstract class NbtContainerDecoder(
    codec: NbtCodec,
) : NbtDecoder(codec) {
    protected var currentIndex: Int = -1

    protected abstract fun selectNextIndex(descriptor: SerialDescriptor): Int

    protected abstract fun currentTag(): Tag

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT,
        PolymorphicKind.OPEN, PolymorphicKind.SEALED,
            -> {
            val compound = currentTag() as CompoundTag
            NbtObjectDecoder(codec, compound)
        }

        StructureKind.LIST -> {
            val list = currentTag() as ListTag
            NbtListDecoder(codec, list)
        }

        StructureKind.MAP -> {
            val compound = currentTag() as CompoundTag
            NbtMapDecoder(codec, compound)
        }

        else -> this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // no-op
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val index = selectNextIndex(descriptor)
        currentIndex = index
        return index
    }

    override fun decodeBoolean(): Boolean = (currentTag() as ByteTag).byteValue().toInt() != 0
    override fun decodeByte(): Byte = (currentTag() as ByteTag).byteValue()
    override fun decodeShort(): Short = (currentTag() as ShortTag).shortValue()
    override fun decodeChar(): Char = (currentTag() as StringTag).asString().getOrNull()?.firstOrNull() ?: '\u0000'
    override fun decodeInt(): Int = (currentTag() as IntTag).intValue()
    override fun decodeLong(): Long = (currentTag() as LongTag).longValue()
    override fun decodeFloat(): Float = (currentTag() as FloatTag).floatValue()
    override fun decodeDouble(): Double = (currentTag() as DoubleTag).doubleValue()
    override fun decodeString(): String = (currentTag() as StringTag).asString().getOrNull() ?: ""
}