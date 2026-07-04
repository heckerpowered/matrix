/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.core.ScheduledExecutor
import heckerpowered.matrix.core.ServerTimeRatio
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import kotlin.time.Duration.Companion.milliseconds
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

        @Volatile
        private var player: ServerPlayer? = null

        /**
         * Pre-migration jar's standalone player tick (borrowed time): a 50ms real-time task
         * that keeps the player entity + connection ticking at ~20Hz on the server thread
         * while the world runs at the warped rate. Started lazily on first standalone warp,
         * never cancelled — old-jar lifecycle verbatim. ServerLevel.tickNonPassenger is the
         * 26.2 name of Yarn's ServerWorld.tickEntity; connection.tick() of networkHandler.tick().
         * Packet responsiveness during warped ticks is owned by MinecraftServerMixin's
         * waitForTasks hook (16a3466), not by this task.
         */
        private val task by lazy {
            ScheduledExecutor.schedule(50.milliseconds) {
                try {
                    val current = player ?: return@schedule
                    val level = current.level() as? ServerLevel ?: return@schedule
                    val server = level.server
                    // Self-clean instead of ticking into a dead state: once the server stops,
                    // submit() runs the lambda INLINE on this pool thread (scheduleExecutables
                    // is false), which would tick the entity off-thread; a removed or
                    // disconnected player would leak the whole ServerLevel through the static.
                    if (server.isStopped || current.isRemoved || current.hasDisconnected()) {
                        player = null
                        return@schedule
                    }
                    server.submit {
                        val serverPlayer = player ?: return@submit
                        if (serverPlayer.isRemoved) {
                            return@submit
                        }
                        (serverPlayer.level() as? ServerLevel)?.tickNonPassenger(serverPlayer)
                        serverPlayer.connection.tick()
                    }
                } catch (e: Exception) {
                    // scheduleAtFixedRate cancels the task permanently on any escaped
                    // throwable — one bad tick must not disable standalone warp for the session.
                    Matrix.LOGGER.error("standalone warp tick failed: ${e.message}", e)
                }
            }
        }
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

        // Time slow can be only used in single player mode.
        player = if (playerStandaloneTick) {
            task
            context.player()
        } else {
            null
        }
    }
}
