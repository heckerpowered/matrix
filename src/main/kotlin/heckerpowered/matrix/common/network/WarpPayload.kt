/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.core.ScheduledExecutor
import heckerpowered.matrix.core.ServerTimeRatio
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.TimeHelper
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

data class WarpPayload(
    private val timeScale: Double,
    private var playerStandaloneTick: Boolean = false,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<WarpPayload> = CustomPayload.id("warp")
        val codec: PacketCodec<PacketByteBuf, WarpPayload> =
            PacketCodec.of(WarpPayload::encode) { buffer ->
                WarpPayload(
                    buffer.readDouble(),
                    buffer.readBoolean()
                )
            }

        private var player: ServerPlayerEntity? = null

        private val task by lazy {
            ScheduledExecutor.schedule(50.milliseconds) {
                player?.apply {
                    (world as? ServerWorld)?.server?.submit {
                        (world as? ServerWorld)?.tickEntity(this)
                        networkHandler.tick()
                    }
                }
            }
        }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeDouble(timeScale)
        buffer.writeBoolean(playerStandaloneTick)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return WarpPayload.id
    }

    fun handle(context: Context) {
        val server = context.server()
        if (server is MinecraftDedicatedServer || !server.isSingleplayer) {
            return
        }

        if (timeScale <= 0) {
            return
        }

        val newTickDurationNanos = ((TimeHelper.SECOND_IN_NANOS / 20L) / timeScale).toLong()
        ServerTimeRatio(server).tickDuration = newTickDurationNanos.nanoseconds

        // Time slow can be only used in single player mode.
        player = if (playerStandaloneTick) {
            task
            context.player()
        } else {
            null
        }
    }
}