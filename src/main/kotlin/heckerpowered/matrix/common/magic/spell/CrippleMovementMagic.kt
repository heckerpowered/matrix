/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.CRIPPLE_MOVEMENT_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.world.ServerWorld

object CrippleMovementMagic : Magic(
    MagicDefinition(
        Matrix.identifier("cripple_movement"),
        6.mana,
        6.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val duration = if (target is PlayerEntity) 20 * 3 else 20 * 10
        val effect = StatusEffectInstance(CRIPPLE_MOVEMENT_EFFECT, duration, 0)
        target.addStatusEffect(effect)

        if (target.world !is ServerWorld) {
            return
        }

        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(CRIPPLE_MOVEMENT_EFFECT) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        if (target?.isInvulnerableToEffect(CRIPPLE_MOVEMENT_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }

    override fun getCost(context: MagicCalculationContext): Long {
        val target = context.target
        if (target is PlayerEntity) {
            return super.getCost(context) * 3
        }

        return super.getCost(context)
    }
}