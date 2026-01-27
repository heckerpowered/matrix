/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.client.player
import heckerpowered.matrix.common.magic.ChannelEntry
import heckerpowered.matrix.common.magic.ChannelExecutor
import heckerpowered.matrix.common.magic.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.MagicManager
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
    private val currentChannelTime: Long = 0L,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ChannelMagicPayload> = CustomPayload.id("channel_magic")
        val codec: PacketCodec<PacketByteBuf, ChannelMagicPayload> =
            PacketCodec.of(ChannelMagicPayload::encode) { buffer ->
                ChannelMagicPayload(
                    buffer.readUuid(),
                    buffer.readInt(),
                    buffer.readLong(),
                    buffer.readLong()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(magicUuid)
        buffer.writeInt(entityId)
        buffer.writeLong(channelTime)
        buffer.writeLong(currentChannelTime)
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

            val cost = magic.getCost(player, entity, player.getChannelQueue(entity))
            val channelEntry = ChannelEntry(magic, cost, channelTime, currentChannelTime)
            ChannelExecutor.channel(channelEntry, player, entity)
            ChannelExecutor.performChannelAnimation(channelEntry, entity, channelTime, currentChannelTime)
        }
    }
}