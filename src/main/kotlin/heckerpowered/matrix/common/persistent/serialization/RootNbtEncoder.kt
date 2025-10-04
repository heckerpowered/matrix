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

class RootNbtEncoder(codec: NbtCodec) : NbtEncoder(codec) {
    companion object {
        private const val ROOT_ELEMENT_NAME = "value"
    }

    val result = NbtCompound()

    override fun encodeBoolean(value: Boolean) = result.putBoolean(ROOT_ELEMENT_NAME, value)
    override fun encodeByte(value: Byte) = result.putByte(ROOT_ELEMENT_NAME, value)
    override fun encodeShort(value: Short) = result.putShort(ROOT_ELEMENT_NAME, value)
    override fun encodeChar(value: Char) = result.putString(ROOT_ELEMENT_NAME, value.toString())
    override fun encodeInt(value: Int) = result.putInt(ROOT_ELEMENT_NAME, value)
    override fun encodeLong(value: Long) = result.putLong(ROOT_ELEMENT_NAME, value)
    override fun encodeFloat(value: Float) = result.putFloat(ROOT_ELEMENT_NAME, value)
    override fun encodeDouble(value: Double) = result.putDouble(ROOT_ELEMENT_NAME, value)
    override fun encodeString(value: String) = result.putString(ROOT_ELEMENT_NAME, value)

    @OptIn(ExperimentalSerializationApi::class)
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT,
        PolymorphicKind.OPEN, PolymorphicKind.SEALED,
            ->
            NbtObjectEncoder(codec, result)

        StructureKind.MAP ->
            NbtMapEncoder(codec, result)

        StructureKind.LIST ->
            NbtListEncoder(codec, NbtList(), result, "value")

        else -> this
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // no-op
    }

    private fun keyOf(descriptor: SerialDescriptor, index: Int) =
        descriptor.getElementName(index)

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        result.putBoolean(keyOf(descriptor, index), value)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        result.putByte(keyOf(descriptor, index), value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        result.putShort(keyOf(descriptor, index), value)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        result.putString(keyOf(descriptor, index), value.toString())
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        result.putInt(keyOf(descriptor, index), value)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        result.putLong(keyOf(descriptor, index), value)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        result.putFloat(keyOf(descriptor, index), value)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        result.putDouble(keyOf(descriptor, index), value)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        result.putString(keyOf(descriptor, index), value)
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        val key = keyOf(descriptor, index)
        return object : Encoder {
            override val serializersModule = codec.serializersModule

            @ExperimentalSerializationApi
            override fun encodeNull() {
                // no-op
            }

            override fun encodeBoolean(value: Boolean) = result.putBoolean(key, value)
            override fun encodeByte(value: Byte) = result.putByte(key, value)
            override fun encodeShort(value: Short) = result.putShort(key, value)
            override fun encodeChar(value: Char) = result.putString(key, value.toString())
            override fun encodeInt(value: Int) = result.putInt(key, value)
            override fun encodeLong(value: Long) = result.putLong(key, value)
            override fun encodeFloat(value: Float) = result.putFloat(key, value)
            override fun encodeDouble(value: Double) = result.putDouble(key, value)
            override fun encodeString(value: String) = result.putString(key, value)
            override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
                val name = enumDescriptor.getElementName(index)
                result.putString(key, name)
            }

            override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
                StructureKind.CLASS, StructureKind.OBJECT, StructureKind.MAP -> {
                    val child = NbtCompound()
                    result.put(key, child)
                    NbtObjectEncoder(codec, child)
                }

                StructureKind.LIST -> {
                    val list = NbtList()
                    result.put(key, list)
                    NbtListEncoder(codec, list)
                }

                else -> error("Inline scalar cannot begin non-structure: ${descriptor.kind}")
            }
        }
    }

    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
        val name = keyOf(descriptor, index)
        when (serializer.descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT, StructureKind.MAP -> {
                val child = NbtCompound()
                result.put(name, child)
                NbtObjectEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            StructureKind.LIST -> {
                val list = NbtList()
                result.put(name, list)
                NbtListEncoder(codec, list)
                    .encodeSerializableValue(serializer, value)
            }

            else -> encodeInlineElement(descriptor, index).encodeSerializableValue(serializer, value)
        }
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
        if (value == null) {
            return
        }

        encodeSerializableElement(descriptor, index, serializer, value)
    }
}