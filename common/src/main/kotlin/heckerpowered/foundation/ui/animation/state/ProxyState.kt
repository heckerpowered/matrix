/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.state

import kotlin.reflect.KProperty

class ProxyState<T>(
    private val getter: () -> T,
    private val setter: (T) -> Unit,
) : MutableState<T> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setter(value)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getter()
    }
}