/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.state

import kotlin.reflect.KProperty

interface State<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
}