/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.magic.MagicManager
import heckerpowered.matrix.common.persistent.ChannelQueue
import heckerpowered.matrix.common.persistent.getChannelSequence
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import java.util.*

class ChannelMagicPayload(
    private val magicUuid: UUID,
    private val entityId: Int,
    private val channelTime: Long,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ChannelMagicPayload> = CustomPayload.id("channel_magic")
        val codec: PacketCodec<PacketByteBuf, ChannelMagicPayload> =
            PacketCodec.of(ChannelMagicPayload::encode) { buffer ->
                ChannelMagicPayload(
                    buffer.readUuid(),
                    buffer.readInt(),
                    buffer.readLong()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(magicUuid)
        buffer.writeInt(entityId)
        buffer.writeLong(channelTime)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ChannelMagicPayload.id
    }

    fun handle(context: Context) {
        context.client().execute {
            val magic = MagicManager.getMagicByUuid(magicUuid) ?: return@execute
            val entity = context.player().world.getEntityById(entityId) ?: return@execute
            if (entity !is LivingEntity) {
                return@execute
            }
            if (ChannelQueue.channelMagic(magic, context.player(), entity, false)) {
                val channelSequence = entity.getChannelSequence(context.player())
                val channelingMagic = channelSequence?.magics?.last() ?: return@execute
                ChannelQueue.channelMagicClient(channelingMagic, entity, channelTime)
            }
        }
    }
}