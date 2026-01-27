/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

/**
 * @author heckerpowered
 */
open class CancellableEvent : Event {
    var isCancelled: Boolean = false
}