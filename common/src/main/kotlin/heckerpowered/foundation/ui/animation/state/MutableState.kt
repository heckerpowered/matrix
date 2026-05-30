/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.state

import kotlin.reflect.KProperty

interface MutableState<T> : State<T> {
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}