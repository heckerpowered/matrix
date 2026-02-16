/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.foundation.ui.animation.time

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class MonotonicTimeline(
    override var duration: Duration,
    var startTimeMark: TimeMark = TimeSource.Monotonic.markNow(),
    override var delay: Duration = Duration.ZERO,
) : Timeline, DelayedStart {
    override fun progress(): Double {
        if (duration <= Duration.ZERO) return 1.0
        val elapsedTime = startTimeMark.elapsedNow().coerceAtLeast(Duration.ZERO)
        val normalized = elapsedTime / duration
        return normalized.coerceIn(0.0, 1.0)
    }

    override fun isFinished(): Boolean {
        if (duration <= Duration.ZERO) return true
        return startTimeMark.elapsedNow() >= duration
    }

    override fun reset() {
        startTimeMark = TimeSource.Monotonic.markNow()
    }
}