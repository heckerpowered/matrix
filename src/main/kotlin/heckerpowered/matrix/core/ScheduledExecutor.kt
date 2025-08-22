/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.core

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit

object ScheduledExecutor {
    private val executor = ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors())

    fun schedule(tickDuration: Duration, task: () -> Unit): ScheduledFuture<*> {
        val future = executor.scheduleAtFixedRate(task, 0, tickDuration.toLong(DurationUnit.NANOSECONDS), TimeUnit.NANOSECONDS)
        return future
    }
}