/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.time

import kotlin.time.Duration

interface Timeline {
    var duration: Duration

    fun progress(): Double
    fun isFinished(): Boolean
    fun reset()
}