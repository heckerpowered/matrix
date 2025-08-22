/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object HealthStealMagic : Magic(MatrixLanguage.magicHealthSteal, 8, MatrixLanguage.magicHealthStealDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
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