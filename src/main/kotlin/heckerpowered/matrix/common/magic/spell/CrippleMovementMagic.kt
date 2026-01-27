/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.CRIPPLE_MOVEMENT_EFFECT
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.channel.ChannelQueue
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicAvailableStatus
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.core.isInvulnerableToEffect
import heckerpowered.matrix.core.common.balance.Accumulator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

object CrippleMovementMagic : Magic(
    MagicDefinition(
        Matrix.identifier("cripple_movement"),
        6.mana,
        6.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: heckerpowered.matrix.common.magic.core.ExecutionPayload) {
        super.cast(player, target, sequence, data)
        if (target is PlayerEntity) {
            target.addStatusEffect(StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, 20 * 3, 0))
            return
        }
        target.addStatusEffect(StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, 20 * 10, 0))
        if (target.world !is ServerWorld) {
            return
        }

        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(CRIPPLE_MOVEMENT_EFFECT) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(
        player: PlayerEntity,
        target: LivingEntity?,
        sequence: ChannelQueue?,
    ): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(CRIPPLE_MOVEMENT_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(player, target, sequence)
    }

    override fun getCost(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?, data: heckerpowered.matrix.common.magic.core.ExecutionPayload, accumulator: Accumulator): Long {
        if (target is PlayerEntity) {
            return super.getCost(player, target, sequence, data, accumulator) * 3
        }

        return super.getCost(player, target, sequence, data, accumulator)
    }
}