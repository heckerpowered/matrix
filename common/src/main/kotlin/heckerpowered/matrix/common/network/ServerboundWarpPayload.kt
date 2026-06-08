/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.ServerTimeRatio
import heckerpowered.matrix.core.ServerTimeWarpLease
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

data class ServerboundWarpPayload(
    private val timeScale: Double,
    private var playerStandaloneTick: Boolean = false,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("warp")
        val type = CustomPacketPayload.Type<ServerboundWarpPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, ServerboundWarpPayload::timeScale,
            ByteBufCodecs.BOOL, ServerboundWarpPayload::playerStandaloneTick,
            ::ServerboundWarpPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        val server = context.server()
        if (!canWarpServer(server)) return
        if (timeScale <= 0) return

        if (timeScale >= 1.0) {
            ServerTimeWarpLease.clear(server)
            ServerTimeRatio.restoreNormalTickDuration(server)
            return
        }

        ServerTimeWarpLease.refresh(server, timeScale)
        val newTickDurationNanos = ((1.seconds.toLong(DurationUnit.NANOSECONDS) / 20L) / timeScale).toLong()
        ServerTimeRatio(server).tickDuration = newTickDurationNanos.nanoseconds

        // TODO: Player standalone tick
    }

    private fun canWarpServer(server: MinecraftServer): Boolean {
        if (!server.isSingleplayer) {
            return false
        }
        val isPublished = runCatching {
            server.javaClass.getMethod("isPublished").invoke(server) as? Boolean
        }.getOrDefault(false)
        return isPublished != true
    }
}
