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

object KillMagic : Magic(MatrixLanguage.killMagic, 1000, MatrixLanguage.killMagicDescription, 20 * 10) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.health = .0f
        val damageSource = MemoryEraseMagic.getDamageSource(player, target, sequence) { player?.damageSources?.playerAttack(player) }
        target.onDeath(damageSource)
    }
}