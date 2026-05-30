/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.MANA_OVERLOAD_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.wipedMagicDamageSource
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.core.SpellRank.BOSS
import heckerpowered.matrix.common.magic.core.SpellRank.CHIMERA
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.network.ClientboundExplosionPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

object ManaOverloadMagic : Magic(
    MagicDefinition(
        Matrix.identifier("magic_overload"),
        10.mana,
        30.ticks
    )
) {
    private const val DURATION_TICKS = 20 * 10
    private const val MAX_LEVEL = 8
    private const val BENEFICIAL_EFFECT_REMOVAL_LEVEL = 2

    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val previousLevel = target.manaOverloadLevel
        val nextLevel = (previousLevel + 1).coerceAtMost(MAX_LEVEL)

        target.applyManaOverload(nextLevel)
        target.applyManaOverloadMilestone(invocation, previousLevel, nextLevel)
        target.syncManaOverloadEffect()
    }

    private val LivingEntity.manaOverloadLevel: Int
        get() {
            val instance = getEffect(MANA_OVERLOAD_EFFECT) ?: return 0
            return instance.amplifier + 1
        }

    private fun LivingEntity.applyManaOverload(level: Int) {
        addEffect(MobEffectInstance(MANA_OVERLOAD_EFFECT, DURATION_TICKS, level - 1, true, false))
    }

    override fun availableStatus(context: MagicCalculationContext): LMagicAvailableStatus {
        val target = context.target
        val effect = target?.getEffect(MANA_OVERLOAD_EFFECT)
        if (target?.isInvulnerableToEffect(MANA_OVERLOAD_EFFECT) == true ||
            (effect?.amplifier ?: 0) >= 7 && (effect?.duration != 0)
        ) {
            return LMagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }

    private fun LivingEntity.applyManaOverloadMilestone(invocation: MagicInvocation, previousLevel: Int, nextLevel: Int) {
        if (BENEFICIAL_EFFECT_REMOVAL_LEVEL in (previousLevel + 1)..nextLevel) {
            removeBeneficialEffects()
        }
        if (nextLevel >= MAX_LEVEL) {
            triggerMaximumManaOverload(invocation)
        }
    }

    private fun LivingEntity.removeBeneficialEffects() {
        activeEffects.removeIf { effectInstance -> effectInstance.effect.value().isBeneficial }
    }

    private fun LivingEntity.triggerMaximumManaOverload(invocation: MagicInvocation) {
        val serverLevel = level() as? ServerLevel ?: return
        hurtServer(serverLevel, invocation.wipedMagicDamageSource(), health)
        for (player in serverLevel.server.playerList.players) {
            ServerPlayNetworking.send(player, ClientboundExplosionPayload(id))
        }
    }

    private fun LivingEntity.syncManaOverloadEffect() {
        val server = level().server ?: return
        val effect = getEffect(MANA_OVERLOAD_EFFECT) ?: return
        for (player in server.playerList.players) {
            player.connection.send(ClientboundUpdateMobEffectPacket(id, effect, false))
        }
    }

    override fun getBaseCost(context: MagicCalculationContext): Long {
        val cost = super.getBaseCost(context)
        return when (context.targetRank()) {
            CHIMERA -> cost - 4
            else -> cost
        }
    }

    override fun getBaseChannelTime(context: MagicCalculationContext): Long {
        val channelTime = super.getBaseChannelTime(context)
        return when (context.targetRank()) {
            BOSS -> channelTime + 2 * 20
            CHIMERA -> channelTime + 4 * 20
            else -> channelTime - 20
        }
    }
}