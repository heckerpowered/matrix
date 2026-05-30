/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.foundation.ui.animation.time.MonotonicTimeline
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class ServerTimeRatio(private val minecraftServer: MinecraftServer) {
    var tickDuration: Duration
        get() = minecraftServer.tickRateManager().nanosecondsPerTick().nanoseconds
        set(value) {
            val newTickDurationNanos = value.toLong(DurationUnit.NANOSECONDS)
            if (minecraftServer.tickRateManager().nanosecondsPerTick() == newTickDurationNanos) {
                // The tick time has already synced.
                return
            }

            minecraftServer.tickRateManager().nanosecondsPerTick = newTickDurationNanos

            val currentTickStartTimeNanos = minecraftServer.tickStartTimeNanos
            val currentTickEndTimeNanos = minecraftServer.tickEndTimeNanos
            val currentTimeNanos = System.nanoTime()
            val timeRange = currentTickStartTimeNanos.toDouble()..currentTickEndTimeNanos.toDouble()
            val remainingTimeRatio = (1 - currentTimeNanos.toDouble().inverseLerp(timeRange)).coerceIn(.0..1.0)
            val newCurrentTickEndTime = currentTimeNanos + (newTickDurationNanos * remainingTimeRatio).toLong()

            // Not waiting for next tick means wait until `tickStartTimeNanos`
            minecraftServer.waitingForNextTick = false
            minecraftServer.nextTickTimeNanos = newCurrentTickEndTime
            minecraftServer.tickEndTimeNanos = newCurrentTickEndTime
        }
}