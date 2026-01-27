/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity

object LevitationMagic : Magic(
    MagicDefinition(
        Matrix.identifier("levitation"),
        30.mana,
        40.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        val statusEffectInstance = target.getStatusEffect(StatusEffects.LEVITATION)
        val amplifier = statusEffectInstance?.amplifier ?: -1
        target.addStatusEffect(StatusEffectInstance(StatusEffects.LEVITATION, 20 * 10, amplifier + 1))
    }
}