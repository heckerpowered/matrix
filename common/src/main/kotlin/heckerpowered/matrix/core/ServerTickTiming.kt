/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer
import java.util.WeakHashMap

object ServerTickTiming {
    private data class TickTiming(
        var startNanos: Long = 0L,
        var endNanos: Long = 0L,
    )

    private val timings = WeakHashMap<MinecraftServer, TickTiming>()

    @JvmStatic
    fun update(server: MinecraftServer, startNanos: Long, endNanos: Long) {
        synchronized(timings) {
            timings.getOrPut(server) { TickTiming() }.also {
                it.startNanos = startNanos
                it.endNanos = endNanos
            }
        }
    }

    fun startNanos(server: MinecraftServer): Long {
        synchronized(timings) {
            return timings[server]?.startNanos ?: System.nanoTime()
        }
    }

    fun endNanos(server: MinecraftServer): Long {
        synchronized(timings) {
            return timings[server]?.endNanos
                ?: (startNanos(server) + server.tickRateManager().nanosecondsPerTick())
        }
    }

    fun setEndNanos(server: MinecraftServer, endNanos: Long) {
        synchronized(timings) {
            timings.getOrPut(server) { TickTiming(startNanos = System.nanoTime()) }.endNanos = endNanos
        }
    }
}
