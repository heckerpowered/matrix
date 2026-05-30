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
import net.minecraft.world.entity.Entity

class ClientboundTeleportPayload(
    val entityId: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val yRot: Float,
    val xRot: Float,
) : CustomPacketPayload {
    constructor(entity: Entity) : this(entity.id, entity.x, entity.y, entity.z, entity.yRot, entity.xRot)

    companion object {
        val payloadId = Matrix.identifier("teleport")
        val type = CustomPacketPayload.Type<ClientboundTeleportPayload>(payloadId)
        val codec = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundTeleportPayload::entityId,
            ByteBufCodecs.DOUBLE, ClientboundTeleportPayload::x,
            ByteBufCodecs.DOUBLE, ClientboundTeleportPayload::y,
            ByteBufCodecs.DOUBLE, ClientboundTeleportPayload::z,
            ByteBufCodecs.FLOAT, ClientboundTeleportPayload::yRot,
            ByteBufCodecs.FLOAT, ClientboundTeleportPayload::xRot,
            ::ClientboundTeleportPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        val level = context.client().level ?: return
        val entity = level.getEntity(entityId) ?: return
        entity.snapTo(x,y,z,yRot,xRot)
        entity.interpolation?.cancel()
    }
}