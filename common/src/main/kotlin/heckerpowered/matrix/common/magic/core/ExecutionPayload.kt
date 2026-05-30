/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.core

import heckerpowered.matrix.common.persistent.serialization.seralizer.MagicReferenceSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Carries per-execution contextual flags for a magic invocation.
 *
 * [ExecutionPayload] represents transient execution metadata attached to a
 * single channeling or casting instance of a magic. It is not part of the magic
 * definition and does not represent persistent player or world state.
 *
 * A payload instance is:
 * - Created when a magic execution is requested or propagated.
 * - Passed through channeling and casting phases.
 * - Allowed to be mutated during channeling to influence later execution.
 *
 * This type is designed to be extensible via subclassing for magic-specific
 * execution data.
 *
 * ### Semantics
 *
 * - [isSpread]
 *
 *   Indicates that this execution was propagated by another magic instance,
 *   rather than being directly initiated by the player.
 *
 *   A spread execution is considered "magic-cast magic" and may bypass or alter
 *   rules that normally apply only to player-initiated actions.
 *
 * - [isSpoofed]
 *
 *   Indicates that this execution should be treated as a spoofed or anonymous
 *   magic.
 *
 *   Spoofed executions are typically produced by effects such as memory wiping
 *   or execution obfuscation. When set, the magic should:
 *   - Be considered untraceable.
 *   - Omit or sanitize damage sources if damage is applied.
 *   - Avoid attributing effects directly to the original caster.
 *
 * ### Lifecycle
 *
 * The payload is part of the committed invocation state and may be copied or
 * propagated when a magic triggers additional executions (for example, via
 * spreading). Implementations should treat the payload as execution-scoped and
 * must not store long-lived references to it.
 *
 * ### Serialization
 *
 * [ExecutionPayload] supports polymorphic serialization to allow magic-specific
 * payload extensions to be transmitted and persisted safely.
 */
@Serializable
@SerialName("ExecutionPayload")
open class ExecutionPayload(
    var isSpread: Boolean = false,
    var isSpoofed: Boolean = false,
) {
    companion object {
        var serializationModule = SerializersModule {
            contextual(Magic::class, MagicReferenceSerializer)
            polymorphic(ExecutionPayload::class) {
                subclass(ExecutionPayload::class, serializer())
            }
        }
    }

    /**
     * Copies execution-relevant flags from another payload instance.
     *
     * This method is intended for propagation scenarios where a new execution
     * should inherit execution semantics from an existing one, while remaining
     * a distinct payload instance.
     *
     * @param source the payload to copy flags from.
     */
    fun inheritFrom(source: ExecutionPayload) {
        this.isSpread = source.isSpread
        this.isSpoofed = source.isSpoofed
    }
}

inline fun <reified T : ExecutionPayload> ExecutionPayload.specialize(factory: () -> T): T {
    return this as? T ?: factory().also { it.inheritFrom(this) }
}