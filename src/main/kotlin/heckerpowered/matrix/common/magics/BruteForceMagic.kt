/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.isInvulnerableToEffect
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity

object BruteForceMagic : Magic(MatrixLanguage.magicBruteForce, 45, MatrixLanguage.magicBruteForceDescription, 40) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.addStatusEffect(StatusEffectInstance(EXPOSED_EFFECT, 200, 0, false, true))
    }

    override fun availableStatus(player: PlayerEntity, target: LivingEntity?, sequence: ChannelSequence?): MagicAvailableStatus {
        if (target?.isInvulnerableToEffect(EXPOSED_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }
        return super.availableStatus(player, target, sequence)
    }
}