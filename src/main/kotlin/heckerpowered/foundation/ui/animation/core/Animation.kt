/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.core

interface Animation<T> {
    fun value(): T
    fun animateTo(value: T, spec: AnimationSpec)
    fun snapTo(value: T)
}