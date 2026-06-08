/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.DurationUnit

object ScheduledExecutor {
    private val threadIndex = AtomicInteger()
    private val executor = ScheduledThreadPoolExecutor(1, ThreadFactory { task ->
        Thread(task, "Matrix scheduled executor ${threadIndex.incrementAndGet()}").apply {
            isDaemon = true
        }
    }).apply {
        removeOnCancelPolicy = true
    }

    fun schedule(tickDuration: Duration, task: () -> Unit): ScheduledFuture<*> {
        val future = executor.scheduleAtFixedRate(task, 0, tickDuration.toLong(DurationUnit.NANOSECONDS), TimeUnit.NANOSECONDS)
        return future
    }
}
