/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import heckerpowered.matrix.mixin.MinecraftServerAccessor
import heckerpowered.matrix.mixin.TickRateManagerAccessor
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
//
// Field access goes through mixin accessors, NOT class-tweaker widenings: in production the
// tweaker's accessible-field entries did not take effect (IllegalAccessError), while mixin
// accessors behave identically in dev and production.
class ServerTimeRatio(private val minecraftServer: MinecraftServer) {
    private val serverAccess get() = minecraftServer as MinecraftServerAccessor
    private val tickRateAccess get() = minecraftServer.tickRateManager() as TickRateManagerAccessor

    var tickDuration: Duration
        get() = tickRateAccess.`matrix$getNanosecondsPerTick`().nanoseconds
        set(value) {
            val newTickDurationNanos = value.toLong(DurationUnit.NANOSECONDS)
            if (tickRateAccess.`matrix$getNanosecondsPerTick`() == newTickDurationNanos) {
                // The tick time has already synced.
                return
            }

            tickRateAccess.`matrix$setNanosecondsPerTick`(newTickDurationNanos)

            val currentTickStartTimeNanos = minecraftServer.tickStartTimeNanos
            val currentTickEndTimeNanos = serverAccess.`matrix$getNextTickTimeNanos`()
            val currentTimeNanos = Util.getNanos()
            val timeRange = currentTickStartTimeNanos.toDouble()..currentTickEndTimeNanos.toDouble()
            val remainingTimeRatio = (1 - currentTimeNanos.toDouble().inverseLerp(timeRange)).coerceIn(.0..1.0)
            val newCurrentTickEndTime = currentTimeNanos + (newTickDurationNanos * remainingTimeRatio).toLong()

            // Not waiting for next tick means wait until `tickStartTimeNanos`
            serverAccess.`matrix$setWaitingForNextTick`(false)
            serverAccess.`matrix$setNextTickTimeNanos`(newCurrentTickEndTime)
        }
}
