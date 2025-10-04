/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

/**
 * @author heckerpowered
 */
open class CancellableEvent : Event {
    var isCancelled: Boolean = false
}