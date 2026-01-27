/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

interface MagicDataSpecification {
    fun serializerModule(): SerializersModule
}

@OptIn(InternalSerializationApi::class)
inline fun <reified T : ExecutionPayload> MagicDataSpecification(
    noinline serializer: () -> KSerializer<T> = { T::class.serializer() },
): MagicDataSpecification = object : MagicDataSpecification {
    override fun serializerModule(): SerializersModule = SerializersModule {
        polymorphic(ExecutionPayload::class) {
            @Suppress("UNCHECKED_CAST")
            subclass(T::class as KClass<ExecutionPayload>, serializer() as KSerializer<ExecutionPayload>)
        }
    }
}