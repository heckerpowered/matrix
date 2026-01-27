/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.ExecutionPayload
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object HealthStealMagic : Magic(
    MagicDefinition(
        Matrix.identifier("health_steal"),
        8.mana,
        20.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        if (player == null) {
            return
        }

        val amount = target.maxHealth * 0.5F
        val healAmount = amount * 0.5F
        player.heal(healAmount)
        player.hungerManager.add(healAmount.toInt(), healAmount)

        if (player.absorptionAmount >= player.maxHealth) {
            return
        }

        val absorptionAmount = (player.absorptionAmount + amount).coerceAtMost(player.maxHealth)
        player.setAbsorptionAmountUnclamped(absorptionAmount)
    }
}