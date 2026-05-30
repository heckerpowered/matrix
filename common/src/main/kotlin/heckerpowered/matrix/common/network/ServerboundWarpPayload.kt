/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.ServerTimeRatio
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.dedicated.DedicatedServer
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
        if (server is DedicatedServer || !server.isSingleplayer) return
        if (timeScale <= 0) return

        val newTickDurationNanos = ((1.seconds.toLong(DurationUnit.NANOSECONDS) / 20L) / timeScale).toLong()
        ServerTimeRatio(server).tickDuration = newTickDurationNanos.nanoseconds

        // TODO: Player standalone tick
    }
}