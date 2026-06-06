/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.channel.ChannelEntry
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.getOrCreateChannelQueue
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.system.Magics
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.LivingEntity
import java.util.*

class ClientboundChannelMagicPayload(
    private val magicUuid: UUID,
    private val entityId: Int,
    private val channelTime: Long,
    private val currentChannelTime: Long = 0L,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("channel_magic")
        val type = CustomPacketPayload.Type<ClientboundChannelMagicPayload>(payloadId)
        val codec = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ClientboundChannelMagicPayload::magicUuid,
            ByteBufCodecs.INT, ClientboundChannelMagicPayload::entityId,
            ByteBufCodecs.LONG, ClientboundChannelMagicPayload::channelTime,
            ByteBufCodecs.LONG, ClientboundChannelMagicPayload::currentChannelTime,
            ::ClientboundChannelMagicPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        context.client().execute {
            val magic = Magics[magicUuid] ?: return@execute
            val player = context.player()
            val entity = player.level().getEntity(entityId) ?: return@execute
            if (entity !is LivingEntity) {
                return@execute
            }

            val calculationContext = MagicCalculationContext(
                caster = CasterContext.fromEntity(player),
                target = entity,
                queue = entity.getOrCreateChannelQueue(player)
            )
            val cost = magic.getCost(calculationContext)
            val entry = ChannelEntry(magic, cost, channelTime, currentChannelTime)

            calculationContext.queue!!.enqueue(entry)
            magic.channel(MagicInvocation.fromEntity(player, entity))
            ChannelExecutor.performChannelAnimation(entry, entity, channelTime, currentChannelTime)
        }
    }
}
