/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

class NbtObjectEncoder(
    codec: NbtCodec,
    private val compound: NbtCompound,
) : NbtEncoder(codec) {
    private fun keyOf(descriptor: SerialDescriptor, index: Int) =
        descriptor.getElementName(index)

    override fun encodeBoolean(value: Boolean) =
        error("NbtObjectEncoder: encodeBoolean() should not be used; use encodeBooleanElement(...)")

    override fun encodeByte(value: Byte) =
        error("NbtObjectEncoder: encodeByte() should not be used; use encodeByteElement(...)")

    override fun encodeShort(value: Short) =
        error("NbtObjectEncoder: encodeShort() should not be used; use encodeShortElement(...)")

    override fun encodeChar(value: Char) =
        error("NbtObjectEncoder: encodeChar() should not be used; use encodeCharElement(...)")

    override fun encodeInt(value: Int) =
        error("NbtObjectEncoder: encodeInt() should not be used; use encodeIntElement(...)")

    override fun encodeLong(value: Long) =
        error("NbtObjectEncoder: encodeLong() should not be used; use encodeLongElement(...)")

    override fun encodeFloat(value: Float) =
        error("NbtObjectEncoder: encodeFloat() should not be used; use encodeFloatElement(...)")

    override fun encodeDouble(value: Double) =
        error("NbtObjectEncoder: encodeDouble() should not be used; use encodeDoubleElement(...)")

    override fun encodeString(value: String) =
        error("NbtObjectEncoder: encodeString() should not be used; use encodeStringElement(...)")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        // no-op
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        compound.putBoolean(keyOf(descriptor, index), value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        compound.putByte(keyOf(descriptor, index), value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        compound.putShort(keyOf(descriptor, index), value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        compound.putString(keyOf(descriptor, index), value.toString())
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        compound.putInt(keyOf(descriptor, index), value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        compound.putLong(keyOf(descriptor, index), value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        compound.putFloat(keyOf(descriptor, index), value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        compound.putDouble(keyOf(descriptor, index), value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        compound.putString(keyOf(descriptor, index), value)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        val name = keyOf(descriptor, index)
        return object : Encoder {
            override val serializersModule = codec.serializersModule

            override fun encodeBoolean(value: Boolean) = compound.putBoolean(name, value)
            override fun encodeByte(value: Byte) = compound.putByte(name, value)
            override fun encodeShort(value: Short) = compound.putShort(name, value)
            override fun encodeChar(value: Char) = compound.putString(name, value.toString())
            override fun encodeInt(value: Int) = compound.putInt(name, value)
            override fun encodeLong(value: Long) = compound.putLong(name, value)
            override fun encodeFloat(value: Float) = compound.putFloat(name, value)
            override fun encodeDouble(value: Double) = compound.putDouble(name, value)
            override fun encodeString(value: String) = compound.putString(name, value)

            @ExperimentalSerializationApi
            override fun encodeNull() {
                // no-op
            }

            override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
                val enumName = enumDescriptor.getElementName(index)
                compound.putString(name, enumName)
            }

            override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

            @OptIn(ExperimentalSerializationApi::class)
            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
                StructureKind.CLASS, StructureKind.OBJECT,
                PolymorphicKind.OPEN, PolymorphicKind.SEALED,
                    -> {
                    val child = NbtCompound()
                    compound.put(name, child)
                    NbtObjectEncoder(codec, child)
                }

                StructureKind.MAP -> {
                    val child = NbtCompound()
                    compound.put(name, child)
                    NbtMapEncoder(codec, child)
                }

                StructureKind.LIST -> {
                    val list = NbtList()
                    compound.put(name, list)
                    NbtListEncoder(codec, list)
                }

                else -> error("Inline scalar cannot begin non-structure: ${descriptor.kind}")
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        if (serializer.descriptor.isInline) {
            encodeInlineElement(descriptor, index)
                .encodeSerializableValue(serializer, value)
            return
        }

        val name = keyOf(descriptor, index)
        when (serializer.descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT,
            PolymorphicKind.OPEN, PolymorphicKind.SEALED,
                -> {
                val child = NbtCompound()
                compound.put(name, child)
                NbtObjectEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            StructureKind.MAP -> {
                val child = NbtCompound()
                compound.put(name, child)
                NbtMapEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            StructureKind.LIST -> {
                val list = NbtList()
                compound.put(name, list)
                NbtListEncoder(codec, list)
                    .encodeSerializableValue(serializer, value)
            }

            else -> {
                encodeInlineElement(descriptor, index).encodeSerializableValue(serializer, value)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        if (value == null) {
            return
        }
        encodeSerializableElement(descriptor, index, serializer, value)
    }
}