/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.core

import net.minecraft.server.MinecraftServer
import java.util.WeakHashMap
import kotlin.time.Duration.Companion.milliseconds

object ServerTimeWarpLease {
    private val leases = WeakHashMap<MinecraftServer, Long>()
    private val task by lazy {
        ScheduledExecutor.schedule(250.milliseconds, ::expireLeases)
    }

    fun refresh(server: MinecraftServer, timeScale: Double) {
        task
        synchronized(leases) {
            if (timeScale >= 1.0) {
                leases.remove(server)
            } else {
                leases[server] = System.nanoTime() + 1_500_000_000L
            }
        }
    }

    @JvmStatic
    fun clear(server: MinecraftServer) {
        synchronized(leases) {
            leases.remove(server)
        }
    }

    private fun expireLeases() {
        val expiredServers = mutableListOf<MinecraftServer>()
        val now = System.nanoTime()
        synchronized(leases) {
            val iterator = leases.entries.iterator()
            while (iterator.hasNext()) {
                val (server, deadlineNanos) = iterator.next()
                if (now >= deadlineNanos) {
                    iterator.remove()
                    expiredServers += server
                }
            }
        }
        expiredServers.forEach { server ->
            runCatching {
                ServerTimeRatio.restoreNormalTickDuration(server)
            }
        }
    }
}
