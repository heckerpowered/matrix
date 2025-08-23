/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.core.AimAssist
import heckerpowered.matrix.client.gameplay.ImminentDanger
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class ImminentDangerPayload(
    val entityId: Int,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ImminentDangerPayload> = CustomPayload.id("imminent_danger")
        val codec: PacketCodec<PacketByteBuf, ImminentDangerPayload> =
            PacketCodec.of(ImminentDangerPayload::encode) { buffer ->
                ImminentDangerPayload(
                    buffer.readInt()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(entityId)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ImminentDangerPayload.id
    }

    @Environment(EnvType.CLIENT)
    fun handle(context: Context) {
        val client = context.client()!!
        val world = client.world ?: return

        val entity = world.getEntityById(entityId) ?: return
        AimAssist.isMouseLocked = true
        AimAssist.autoUnlock = true
        AimAssist.autoApplyRotation = true
        AimAssist.lookAt(entity.pos.add(.0, -0.5, .0), client.renderTickCounter.getTickDelta(true).toDouble())
        ImminentDanger.trackedEntity = entity
    }
}