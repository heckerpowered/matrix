/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.MANA_OVERLOAD_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.network.ExplosionPayload
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.world.ServerWorld

object ManaOverloadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("magic_overload"),
        4.mana,
        6.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val manaOverloadInstance = target.getStatusEffect(MANA_OVERLOAD_EFFECT)

        val nextAmplifier = (manaOverloadInstance?.amplifier ?: -1) + 1
        target.addStatusEffect(
            StatusEffectInstance(
                MANA_OVERLOAD_EFFECT,
                20 * 10,
                nextAmplifier.coerceAtMost(7),
                true, false
            )
        )
        when (nextAmplifier) {
            1 -> {
                target.statusEffects
                    .filter { it.effectType.value().isBeneficial }
                    .map { it.effectType }
                    .forEach { target.removeStatusEffect(it) }
            }

            7 -> {
                target.damage(target.health, target.damageSources.create(MatrixDamageTypes.magic))
                if (target.world is ServerWorld) {
                    target.world.server?.playerManager?.playerList?.forEach {
                        ServerPlayNetworking.send(it, ExplosionPayload(target.id))
                    }
                }
            }
        }

        val server = target.world.server ?: return
        val statusEffectInstance = target.getStatusEffect(MANA_OVERLOAD_EFFECT) ?: return
        for (serverPlayer in server.playerManager.playerList) {
            serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
        }
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        val effect = target?.getStatusEffect(MANA_OVERLOAD_EFFECT)
        if (target?.isInvulnerableToEffect(MANA_OVERLOAD_EFFECT) == true ||
            (effect?.amplifier ?: 0) >= 7 && (effect?.duration != 0)
        ) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }
}