package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class ServerTimeRatio(private val minecraftServer: MinecraftServer) {
    var tickDuration: Duration
        get() = minecraftServer.tickManager.nanosPerTick.nanoseconds
        set(value) {
            val newTickDurationNanos = value.toLong(DurationUnit.NANOSECONDS)
            if (minecraftServer.tickManager.nanosPerTick == newTickDurationNanos) {
                // The tick time has already synced.
                return
            }

            minecraftServer.tickManager.nanosPerTick = newTickDurationNanos

            val currentTickStartTimeNanos = minecraftServer.matrixTickStartTimeNanos
            val currentTickEndTimeNanos = minecraftServer.matrixTickEndTimeNanos
            val currentTimeNanos = Util.getMeasuringTimeNano()
            val timeRange = currentTickStartTimeNanos.toDouble()..currentTickEndTimeNanos.toDouble()
            val remainingTimeRatio = (1 - currentTimeNanos.toDouble().inverseLerp(timeRange)).coerceIn(.0..1.0)
            val newCurrentTickEndTime = currentTimeNanos + (newTickDurationNanos * remainingTimeRatio).toLong()

            // Not waiting for next tick means wait until `tickStartTimeNanos`
            minecraftServer.waitingForNextTick = false
            minecraftServer.tickStartTimeNanos = newCurrentTickEndTime
        }
}