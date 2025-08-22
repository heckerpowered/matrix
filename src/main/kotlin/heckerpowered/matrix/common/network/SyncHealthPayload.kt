/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

/**
 * When the time is slowed down, the synchronization of health is slowed down as well.
 */
class SyncHealthPayload(
    private val health: Float,
    private val absorptionAmount: Float,
) : CustomPayload {
    constructor(player: PlayerEntity) : this(player.health, player.absorptionAmount)

    companion object {
        val id: CustomPayload.Id<SyncHealthPayload> = CustomPayload.id("sync_health")
        val codec: PacketCodec<PacketByteBuf, SyncHealthPayload> =
            PacketCodec.of(SyncHealthPayload::encode) { buffer ->
                SyncHealthPayload(
                    buffer.readFloat(),
                    buffer.readFloat()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeFloat(health)
        buffer.writeFloat(absorptionAmount)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return SyncHealthPayload.id
    }

    fun handle(context: Context) {
        val player = context.player()
        player.health = health
        player.setAbsorptionAmountUnclamped(absorptionAmount)
    }
}