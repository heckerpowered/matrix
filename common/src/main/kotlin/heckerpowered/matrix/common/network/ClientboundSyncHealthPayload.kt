/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.player.Player

/**
 * When the time is slowed down, the synchronization of health is slowed down as well.
 */
class ClientboundSyncHealthPayload(
    private val health: Float,
    private val absorptionAmount: Float,
) : CustomPacketPayload {
    constructor(player: Player) : this(player.health, player.absorptionAmount)

    companion object {
        val payloadId = Matrix.identifier("sync_health")
        val type = CustomPacketPayload.Type<ClientboundSyncHealthPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundSyncHealthPayload::health,
            ByteBufCodecs.FLOAT, ClientboundSyncHealthPayload::absorptionAmount,
            ::ClientboundSyncHealthPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        val player = context.player()
        player.health = health
        player.absorptionAmount = absorptionAmount // TODO: Set absorption amount unclamped
    }
}