/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.channel

import heckerpowered.matrix.client.render.ChannelAnimation
import heckerpowered.matrix.client.render.ChannelSequenceRenderer
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailability
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.network.ClientboundChannelMagicPayload
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

object ChannelExecutor {
    fun channel(magic: Magic, invocation: MagicInvocation, attempt: ExecutionPolicy = ExecutionPolicy()): MagicAvailability {
        val caster = invocation.caster
        val target = invocation.target
        val queue = invocation.queue

        val casterEntity = caster.entityOrNull() ?: return MagicAvailability(MagicAvailableStatus.Unavailable)
        val player = casterEntity as? ServerPlayer ?: return MagicAvailability(MagicAvailableStatus.Unavailable)
        val calculationContext = MagicCalculationContext.fromInvocation(invocation)

        val available = magic.availableStatus(calculationContext)
        if (!attempt.isMagicAvailable(available)) {
            val rejectedStatus = if (available.isAvailable)
                MagicAvailability(MagicAvailableStatus.Unavailable) else available
            return rejectedStatus
        }

        val cost = magic.getCost(calculationContext)
        if (!attempt.payCost(magic, cost.mana, invocation)) {
            return MagicAvailability(MagicAvailableStatus.InsufficientMana)
        }

        val channelTime = magic.getChannelTime(calculationContext)
        val payload = invocation.payload
        val entry = ChannelEntry(
            magic = magic,
            cost = cost,
            channelTime = channelTime,
            payload = payload
        )

        queue.enqueue(entry)
        entry.magic.channel(invocation)

        val channelPayload = ClientboundChannelMagicPayload(magic.definition.uuid, target.id, channelTime)
        ServerPlayNetworking.send(player, channelPayload)
        return MagicAvailability()
    }

    @Environment(EnvType.CLIENT)
    fun performChannelAnimation(magic: ChannelEntry, target: LivingEntity, channelTime: Long = magic.channelTime, currentChannelTime: Long = 0L) {
        ChannelSequenceRenderer
            .channelSequenceAnimationMap
            .computeIfAbsent(target) { mutableListOf() }
            .add(ChannelAnimation(magic.magic).also {
                it.channelTime = channelTime
                it.currentChannelTime = currentChannelTime
                it.initialProgressOffset = Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(true)
            })
        ChannelSequenceRenderer.offsetAnimationMap
            .computeIfAbsent(target) { ChannelSequenceRenderer.OffsetAnimation() }
    }
}
