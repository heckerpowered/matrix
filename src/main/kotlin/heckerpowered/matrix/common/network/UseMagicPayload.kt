/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.network

import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.magic.channel.ChannelExecutor
import heckerpowered.matrix.common.magic.system.MagicManager
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import java.util.*

data class UseMagicPayload(
    private val uuid: UUID,
    private val entityId: Int,
) : CustomPayload {
    companion object {
        val id: CustomPayload.Id<UseMagicPayload> = CustomPayload.id("use_magic")
        val codec: PacketCodec<PacketByteBuf, UseMagicPayload> =
            PacketCodec.of(UseMagicPayload::encode) { buffer ->
                UseMagicPayload(
                    buffer.readUuid(),
                    buffer.readInt()
                )
            }
    }

    private fun encode(buffer: PacketByteBuf) {
        buffer.writeUuid(uuid)
        buffer.writeInt(entityId)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return UseMagicPayload.id
    }

    fun handle(context: Context) {
        val player = context.player()

        val targetedEntity = context.player().world.getEntityById(entityId) ?: return
        if (targetedEntity !is LivingEntity) {
            return
        }

        val magic = MagicManager.getMagicByUuid(uuid) ?: return
        val wizardHelmet = player.wizardHelmet
        if ((wizardHelmet.item as? WizardHelmet)?.hasMagic(wizardHelmet, magic) != true) {
            return
        }
        ChannelExecutor.channel(magic, player, targetedEntity)
        context.responseSender().sendPacket(SyncHealthPayload(player))
    }
}