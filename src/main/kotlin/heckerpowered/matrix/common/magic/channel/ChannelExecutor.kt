/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.ChannelAnimation
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.network.ChannelMagicPayload
import heckerpowered.matrix.core.MatrixLivingEntity
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object ChannelExecutor {
    fun channel(magic: Magic, channeler: ServerPlayerEntity, target: LivingEntity, channelRequest: ChannelRequest = ChannelRequest()): MagicAvailableStatus {
        check(target is MatrixLivingEntity)

        val queues = target.getChannelQueues()
        val queue = queues.computeIfAbsent(channeler.uuid) {
            ChannelQueue(channeler, channeler.uuid, target)
        }

        val data = channelRequest.data
        val channelTime = magic.getChannelTime(channeler, target, queue, data)
        val cost = magic.getCost(channeler, target, queue, data)
        val convertRatio = magic.getBloodPactConvertRatio(channeler, target, queue, data)

        val available = magic.availableStatus(channeler, target, queue)
        if (!channelRequest.isMagicAvailable(available)) {
            return available
        }

        if (!channelRequest.payCost(channeler, cost.mana, convertRatio)) {
            return MagicAvailableStatus.AVAILABLE_MANA_NOT_ENOUGH
        }

        val channelEntry = ChannelEntry(magic, cost, channelTime, data = channelRequest.data)
        channel(channelEntry, channeler, target)
        ServerPlayNetworking.send(channeler, ChannelMagicPayload(magic.definition.uuid, target.id, channelTime))
        return MagicAvailableStatus.AVAILABLE
    }

    fun channel(magic: ChannelEntry, channeler: Channeler, target: LivingEntity) {
        check(target is MatrixLivingEntity)

        val queues = target.getChannelQueues()
        val queue = queues.computeIfAbsent(channeler.uuid) {
            ChannelQueue(channeler, channeler.uuid, target)
        }

        queue.enqueue(magic)
        magic.magic.channel(player, target, queue, magic.data)
    }

    @Environment(EnvType.CLIENT)
    fun performChannelAnimation(magic: ChannelEntry, target: LivingEntity, channelTime: Long = magic.channelTime, currentChannelTime: Long = 0L) {
        ChannelSequenceRenderer
            .channelSequenceAnimationMap
            .computeIfAbsent(target) { mutableListOf() }
            .add(ChannelAnimation(magic.magic).also {
                it.channelTime = channelTime
                it.currentChannelTime = currentChannelTime
                it.initialProgressOffset = minecraft.getRenderTickCounter().getTickDelta(true)
            })
        ChannelSequenceRenderer.offsetAnimationMap
            .computeIfAbsent(target) { ChannelSequenceRenderer.Companion.OffsetAnimation() }
    }
}