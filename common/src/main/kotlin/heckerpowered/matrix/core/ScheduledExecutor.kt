/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit

object ScheduledExecutor {
    private val executor = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors()) { runnable ->
        // Daemon threads: an armed fixed-rate task (standalone warp tick) must never keep the
        // JVM alive after a normal game quit — the default factory's non-daemon workers would.
        Thread(runnable, "matrix-scheduler").apply { isDaemon = true }
    }

    fun schedule(tickDuration: Duration, task: () -> Unit): ScheduledFuture<*> {
        val future = executor.scheduleAtFixedRate(task, 0, tickDuration.toLong(DurationUnit.NANOSECONDS), TimeUnit.NANOSECONDS)
        return future
    }
}