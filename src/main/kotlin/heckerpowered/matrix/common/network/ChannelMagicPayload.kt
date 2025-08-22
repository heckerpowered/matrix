/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.MagicManager
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.common.persistent.getChannelSequence
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class ChannelMagicPayload(
    private val magicId: Int,
    private val entityId: Int,
    private val channelTime: Long,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<ChannelMagicPayload> = CustomPayload.id("channel_magic")
        val codec: PacketCodec<PacketByteBuf, ChannelMagicPayload> =
            PacketCodec.of(ChannelMagicPayload::encode) { buffer ->
                ChannelMagicPayload(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readLong()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeInt(magicId)
        buffer.writeInt(entityId)
        buffer.writeLong(channelTime)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ChannelMagicPayload.id
    }

    fun handle(context: Context) {
        context.client().execute {
            val magic = MagicManager.getMagicById(magicId) ?: return@execute
            val entity = context.player().world.getEntityById(entityId) ?: return@execute
            if (entity !is LivingEntity) {
                return@execute
            }
            if (ChannelSequence.channelMagic(magic, context.player(), entity, false)) {
                val channelSequence = entity.getChannelSequence(context.player())
                val channelingMagic = channelSequence?.magics?.last() ?: return@execute
                ChannelSequence.channelMagicClient(channelingMagic, entity, channelTime)
            }
        }
    }
}