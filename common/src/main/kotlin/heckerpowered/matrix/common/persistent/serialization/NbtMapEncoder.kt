/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.persistent.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class NbtMapEncoder(
    codec: NbtCodec,
    private val out: CompoundTag,
) : NbtEncoder(codec) {
    private var expectKey = true
    private var pendingKey: String? = null

    override fun encodeBoolean(value: Boolean): Unit =
        error("NbtMapEncoder: encodeBoolean() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeByte(value: Byte): Unit =
        error("NbtMapEncoder: encodeByte() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeShort(value: Short): Unit =
        error("NbtMapEncoder: encodeShort() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeChar(value: Char): Unit =
        error("NbtMapEncoder: encodeChar() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeInt(value: Int): Unit =
        error("NbtMapEncoder: encodeInt() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeLong(value: Long): Unit =
        error("NbtMapEncoder: encodeLong() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeFloat(value: Float): Unit =
        error("NbtMapEncoder: encodeFloat() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeDouble(value: Double): Unit =
        error("NbtMapEncoder: encodeDouble() not allowed; use encode*Element/encodeInlineElement")

    override fun encodeString(value: String): Unit =
        error("NbtMapEncoder: encodeString() not allowed; use encode*Element/encodeInlineElement")

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        // no-op
        pendingKey = null
        expectKey = true
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        ensureValuePos("Boolean")
        putValue { k -> out.putBoolean(k, value) }
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        ensureValuePos("Byte")
        putValue { k -> out.putByte(k, value) }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        ensureValuePos("Short")
        putValue { k -> out.putShort(k, value) }
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        ensureValuePos("Char")
        putValue { k -> out.putString(k, value.toString()) }
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        ensureValuePos("Int")
        putValue { k -> out.putInt(k, value) }
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        ensureValuePos("Long")
        putValue { k -> out.putLong(k, value) }
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        ensureValuePos("Float")
        putValue { k -> out.putFloat(k, value) }
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        ensureValuePos("Double")
        putValue { k -> out.putDouble(k, value) }
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (expectKey) {
            pendingKey = value
            expectKey = false
        } else {
            putValue { k -> out.putString(k, value) }
        }
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return object : Encoder {
            override val serializersModule = codec.serializersModule

            override fun encodeString(value: String) {
                if (expectKey) {
                    pendingKey = value
                    expectKey = false
                } else {
                    putValue { k -> out.putString(k, value) }
                }
            }

            override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
                val name = enumDescriptor.getElementName(index)
                if (expectKey) {
                    pendingKey = name
                    expectKey = false
                } else {
                    putValue { k -> out.putString(k, name) }
                }
            }

            @ExperimentalSerializationApi
            override fun encodeNull() {
                if (expectKey) {
                    throw SerializationException("Map key cannot be null")
                } else {
                    pendingKey = null
                    expectKey = true
                }
            }

            override fun encodeBoolean(value: Boolean) {
                ensureValuePos("Boolean")
                putValue { k -> out.putBoolean(k, value) }
            }

            override fun encodeByte(value: Byte) {
                ensureValuePos("Byte")
                putValue { k -> out.putByte(k, value) }
            }

            override fun encodeShort(value: Short) {
                ensureValuePos("Short")
                putValue { k -> out.putShort(k, value) }
            }

            override fun encodeChar(value: Char) {
                ensureValuePos("Char")
                putValue { k -> out.putString(k, value.toString()) }
            }

            override fun encodeInt(value: Int) {
                ensureValuePos("Int")
                putValue { k -> out.putInt(k, value) }
            }

            override fun encodeLong(value: Long) {
                ensureValuePos("Long")
                putValue { k -> out.putLong(k, value) }
            }

            override fun encodeFloat(value: Float) {
                ensureValuePos("Float")
                putValue { k -> out.putFloat(k, value) }
            }

            override fun encodeDouble(value: Double) {
                ensureValuePos("Double")
                putValue { k -> out.putDouble(k, value) }
            }

            override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

            @OptIn(ExperimentalSerializationApi::class)
            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
                if (expectKey) {
                    error("Map key must be String/Enum; got structure: $descriptor")
                }
                val key = pendingKey ?: error("Map value encoded before key")
                pendingKey = null
                expectKey = true

                return when (descriptor.kind) {
                    StructureKind.CLASS, StructureKind.OBJECT,
                    PolymorphicKind.OPEN, PolymorphicKind.SEALED,
                        -> {
                        val child = CompoundTag()
                        out.put(key, child)
                        NbtObjectEncoder(codec, child)
                    }

                    StructureKind.MAP -> {
                        val child = CompoundTag()
                        out.put(key, child)
                        NbtMapEncoder(codec, child)
                    }

                    StructureKind.LIST -> {
                        val list = ListTag()
                        out.put(key, list)
                        NbtListEncoder(codec, list)
                    }

                    else -> error("Unsupported structure in map value: ${descriptor.kind}")
                }
            }
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        encodeInlineElement(descriptor, index).encodeSerializableValue(serializer, value)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        if (value == null) {
            encodeInlineElement(descriptor, index).encodeNull()
            return
        }
        encodeSerializableElement(descriptor, index, serializer, value)
    }

    private fun ensureValuePos(typeName: String) {
        if (expectKey) {
            throw SerializationException("Map key must be String/Enum; got $typeName")
        }
    }

    private inline fun putValue(put: (key: String) -> Unit) {
        val key = pendingKey ?: error("Map value encoded before key")
        put(key)
        pendingKey = null
        expectKey = true
    }
}