/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

/**
 * Describes how to match and deliver events for a registration.
 *
 * @property stopOnCancel if true and the event is [CancellableEvent] set to cancelled, stop dispatch.
 * @property includeSupertypes if true, listeners for supertypes (interfaces/classes) also receive subtypes.
 */
data class EventPolicy<T>(
    val stopOnCancel: Boolean = true,
    val includeSupertypes: Boolean = true,
)