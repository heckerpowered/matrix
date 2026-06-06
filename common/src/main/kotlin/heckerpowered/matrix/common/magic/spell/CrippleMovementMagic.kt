/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.player.Player

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
        val duration = if (target is Player) 20 * 3 else 20 * 10
        val effect = MobEffectInstance(ModMobEffects.CrippleMovement, duration, 0)
        target.addEffect(effect)

        val server = target.level().server ?: return
        val statusEffectInstance = target.getEffect(ModMobEffects.CrippleMovement) ?: return
        for (serverPlayer in server.playerList.players) {
            serverPlayer.connection.send(ClientboundUpdateMobEffectPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailability {
        val availability = super.availableStatus(context)

        val target = context.target
        if (target?.isInvulnerableToEffect(ModMobEffects.CrippleMovement) == true) {
            availability += MagicAvailableStatus.TargetImmune
        }

        return availability
    }

    override fun getCost(context: MagicCalculationContext): Long {
        val target = context.target
        if (target is Player) {
            return super.getCost(context) * 3
        }

        return super.getCost(context)
    }
}