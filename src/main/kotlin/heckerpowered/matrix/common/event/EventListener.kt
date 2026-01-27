/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

fun interface EventListener<T> {
    fun onEvent(event: T)
}