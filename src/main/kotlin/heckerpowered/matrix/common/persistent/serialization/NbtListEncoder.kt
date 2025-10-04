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
import net.minecraft.nbt.*

class NbtListEncoder(
    codec: NbtCodec,
    private val list: NbtList,
    private val parent: NbtCompound? = null,
    private val fieldName: String? = null,
) : NbtEncoder(codec) {
    override fun encodeBoolean(value: Boolean) {
        list.add(NbtByte.of(if (value) 1 else 0))
    }

    override fun encodeByte(value: Byte) {
        list.add(NbtByte.of(value))
    }

    override fun encodeShort(value: Short) {
        list.add(NbtShort.of(value))
    }

    override fun encodeChar(value: Char) {
        list.add(NbtString.of(value.toString()))
    }

    override fun encodeInt(value: Int) {
        list.add(NbtInt.of(value))
    }

    override fun encodeLong(value: Long) {
        list.add(NbtLong.of(value))
    }

    override fun encodeFloat(value: Float) {
        list.add(NbtFloat.of(value))
    }

    override fun encodeDouble(value: Double) {
        list.add(NbtDouble.of(value))
    }

    override fun encodeString(value: String) {
        list.add(NbtString.of(value))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        if (parent != null && fieldName != null && !parent.contains(fieldName)) {
            parent.put(fieldName, list)
        }
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        list.add(NbtByte.of(if (value) 1 else 0))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        list.add(NbtByte.of(value))
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        list.add(NbtShort.of(value))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        list.add(NbtString.of(value.toString()))
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        list.add(NbtInt.of(value))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        list.add(NbtLong.of(value))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        list.add(NbtFloat.of(value))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        list.add(NbtDouble.of(value))
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        list.add(NbtString.of(value))
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return object : Encoder {
            override val serializersModule = codec.serializersModule

            override fun encodeBoolean(value: Boolean) {
                list.add(NbtByte.of(if (value) 1 else 0))
            }

            override fun encodeByte(value: Byte) {
                list.add(NbtByte.of(value))
            }

            override fun encodeShort(value: Short) {
                list.add(NbtShort.of(value))
            }

            override fun encodeChar(value: Char) {
                list.add(NbtString.of(value.toString()))
            }

            override fun encodeInt(value: Int) {
                list.add(NbtInt.of(value))
            }

            override fun encodeLong(value: Long) {
                list.add(NbtLong.of(value))
            }

            override fun encodeFloat(value: Float) {
                list.add(NbtFloat.of(value))
            }

            override fun encodeDouble(value: Double) {
                list.add(NbtDouble.of(value))
            }

            override fun encodeString(value: String) {
                list.add(NbtString.of(value))
            }

            @ExperimentalSerializationApi
            override fun encodeNull() {
                error("NBT List does not support null elements")
            }

            override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
                val name = enumDescriptor.getElementName(index)
                list.add(NbtString.of(name))
            }

            override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

            @OptIn(ExperimentalSerializationApi::class)
            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
                when (descriptor.kind) {
                    StructureKind.CLASS, StructureKind.OBJECT,
                    PolymorphicKind.OPEN, PolymorphicKind.SEALED,
                        -> {
                        val child = NbtCompound()
                        list.add(child)
                        NbtObjectEncoder(codec, child)
                    }

                    StructureKind.MAP -> {
                        val child = NbtCompound()
                        list.add(child)
                        NbtMapEncoder(codec, child)
                    }

                    StructureKind.LIST -> {
                        val child = NbtList()
                        list.add(child)
                        NbtListEncoder(codec, child)
                    }

                    else -> error("Inline scalar element cannot begin non-structure: ${descriptor.kind}")
                }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> encodeSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T) {
        if (serializer.descriptor.isInline) {
            encodeInlineElement(descriptor, index)
                .encodeSerializableValue(serializer, value)
            return
        }

        when (serializer.descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT,
            PolymorphicKind.OPEN, PolymorphicKind.SEALED,
                -> {
                val child = NbtCompound()
                list.add(child)
                NbtObjectEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            StructureKind.MAP -> {
                val child = NbtCompound()
                list.add(child)
                NbtMapEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            StructureKind.LIST -> {
                val child = NbtList()
                list.add(child)
                NbtListEncoder(codec, child)
                    .encodeSerializableValue(serializer, value)
            }

            else -> encodeInlineElement(descriptor, index).encodeSerializableValue(serializer, value)
        }
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(descriptor: SerialDescriptor, index: Int, serializer: SerializationStrategy<T>, value: T?) {
        if (value == null) {
            error("NBT List does not support null elements at index $index of ${descriptor.serialName}")
        }
        encodeSerializableElement(descriptor, index, serializer, value)
    }
}