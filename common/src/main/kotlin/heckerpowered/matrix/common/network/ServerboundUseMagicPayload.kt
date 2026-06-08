/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.common.persistent.isInfiniteMana
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.entity.LivingEntity
import java.util.UUID

data class ServerboundUseMagicPayload(
    private val uuid: UUID,
    private val entityId: Int,
) : CustomPacketPayload {
    companion object {
        val payloadId = Matrix.identifier("use_magic")
        val type = CustomPacketPayload.Type<ServerboundUseMagicPayload>(payloadId)
        val codec = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ServerboundUseMagicPayload::uuid,
            ByteBufCodecs.INT, ServerboundUseMagicPayload::entityId,
            ::ServerboundUseMagicPayload
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return type
    }

    fun handle(context: Context) {
        val player = context.player()

        val targetedEntity = context.player().level().getEntity(entityId) ?: return
        if (targetedEntity !is LivingEntity) {
            return
        }

        val wizardHelmetStack = player.wizardHelmetStack
        val magic = Magics[uuid] ?: return
        if (!player.isInfiniteMana && player.wizardHelmet?.hasMagic(player, wizardHelmetStack, magic) != true) {
            return
        }

        val invocation = MagicInvocation.fromEntity(player, targetedEntity)
        val result = ChannelExecutor.channel(magic, invocation)
        if (!result.isAvailable) {
            @Suppress("LoggingStringTemplateAsArgument")
            Matrix.LOGGER.debug("Magic channel failed: $result")
        }
        context.responseSender().sendPacket(ClientboundSyncHealthPayload(player))
    }
}
