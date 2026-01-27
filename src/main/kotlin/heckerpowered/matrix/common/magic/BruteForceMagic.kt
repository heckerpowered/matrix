/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object BruteForceMagic : Magic(
    MagicDefinition(
        Matrix.identifier("brute_force"),
        45.mana,
        40.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.addStatusEffect(StatusEffectInstance(EXPOSED_EFFECT, 200, 0, false, true))
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelQueue?): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(EXPOSED_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }
        return super.availableStatus(player, target, sequence)
    }
}