/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.state

import heckerpowered.foundation.ui.animation.core.Animation
import heckerpowered.foundation.ui.animation.core.AnimationScope
import kotlin.reflect.KProperty

class AnimatedState<T> internal constructor(
    private val scope: AnimationScope,
    var animation: Animation<T>,
) : MutableState<T> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val spec = scope.currentSpec
        if (spec != null) {
            animation.animateTo(value, spec)
        } else {
            animation.snapTo(value)
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return animation.value()
    }
}