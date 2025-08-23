/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.magics

import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.common.entity.AttractorEntity
import heckerpowered.matrix.common.persistent.ChannelSequence
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity

object AttractMagic : Magic(MatrixLanguage.magicAttract, 20, MatrixLanguage.magicAttractDescription, 20) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelSequence, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.world.spawnEntity(AttractorEntity(target.world).also {
            it.setPosition(target.pos)
            it.owner = player
        })
    }
}