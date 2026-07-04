/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

// Restored from 16a3466 ("Fix time warp"): the direct nanosecondsPerTick write bypasses
// setTickRate's >= 1 tps clamp (0.01x slow-time needs 0.2 tps), and all deadline math uses
// Util.getNanos() — the server loop's clock, which the client JVM redirects to the GLFW
// timer (System.nanoTime() has a different epoch and would produce a never-arriving
// deadline, freezing the integrated server).
class ServerTimeRatio(private val minecraftServer: MinecraftServer) {
    var tickDuration: Duration
        get() = minecraftServer.tickRateManager().nanosecondsPerTick.nanoseconds
        set(value) {
            val newTickDurationNanos = value.toLong(DurationUnit.NANOSECONDS)
            if (minecraftServer.tickRateManager().nanosecondsPerTick == newTickDurationNanos) {
                // The tick time has already synced.
                return
            }

            minecraftServer.tickRateManager().nanosecondsPerTick = newTickDurationNanos

            val currentTickStartTimeNanos = minecraftServer.tickStartTimeNanos
            val currentTickEndTimeNanos = minecraftServer.nextTickTimeNanos
            val currentTimeNanos = Util.getNanos()
            val timeRange = currentTickStartTimeNanos.toDouble()..currentTickEndTimeNanos.toDouble()
            val remainingTimeRatio = (1 - currentTimeNanos.toDouble().inverseLerp(timeRange)).coerceIn(.0..1.0)
            val newCurrentTickEndTime = currentTimeNanos + (newTickDurationNanos * remainingTimeRatio).toLong()

            // Not waiting for next tick means wait until `tickStartTimeNanos`
            minecraftServer.waitingForNextTick = false
            minecraftServer.nextTickTimeNanos = newCurrentTickEndTime
        }
}
