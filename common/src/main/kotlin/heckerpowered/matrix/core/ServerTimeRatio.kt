/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Util
import java.util.concurrent.locks.LockSupport
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class ServerTimeRatio(private val minecraftServer: MinecraftServer) {
    companion object {
        private const val NORMAL_TICK_DURATION_NANOS = 50_000_000L

        @JvmStatic
        fun restoreNormalTickDuration(minecraftServer: MinecraftServer) {
            applyNormalTickDuration(minecraftServer)
            if (Thread.currentThread() != minecraftServer.runningThread && !minecraftServer.isStopped) {
                runCatching {
                    minecraftServer.executeIfPossible {
                        applyNormalTickDuration(minecraftServer)
                    }
                }
            }
        }

        private fun applyNormalTickDuration(minecraftServer: MinecraftServer) {
            minecraftServer.tickRateManager().nanosecondsPerTick = NORMAL_TICK_DURATION_NANOS
            val now = Util.getNanos()
            minecraftServer.waitingForNextTick = false
            minecraftServer.nextTickTimeNanos = now
            ServerTickTiming.update(minecraftServer, now, now + NORMAL_TICK_DURATION_NANOS)
            LockSupport.unpark(minecraftServer.runningThread)
        }
    }

    var tickDuration: Duration
        get() = minecraftServer.tickRateManager().nanosecondsPerTick().nanoseconds
        set(value) {
            val newTickDurationNanos = value.toLong(DurationUnit.NANOSECONDS)
            if (minecraftServer.tickRateManager().nanosecondsPerTick() == newTickDurationNanos) {
                // The tick time has already synced.
                return
            }

            minecraftServer.tickRateManager().nanosecondsPerTick = newTickDurationNanos

            val currentTickStartTimeNanos = ServerTickTiming.startNanos(minecraftServer)
            val currentTickEndTimeNanos = ServerTickTiming.endNanos(minecraftServer)
            val currentTimeNanos = Util.getNanos()
            val timeRange = currentTickStartTimeNanos.toDouble()..currentTickEndTimeNanos.toDouble()
            val remainingTimeRatio = (1 - currentTimeNanos.toDouble().inverseLerp(timeRange)).coerceIn(.0..1.0)
            val newCurrentTickEndTime = currentTimeNanos + (newTickDurationNanos * remainingTimeRatio).toLong()

            // Not waiting for next tick means wait until the next tick time.
            minecraftServer.waitingForNextTick = false
            minecraftServer.nextTickTimeNanos = newCurrentTickEndTime
            ServerTickTiming.setEndNanos(minecraftServer, newCurrentTickEndTime)
            LockSupport.unpark(minecraftServer.runningThread)
        }
}
