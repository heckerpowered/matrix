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

object TargetPositioningMagic : Magic(
    MagicDefinition(
        Matrix.identifier("target_positioning"),
        4.mana,
        20.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: MagicData) {
        super.cast(player, target, sequence, data)
        target.world.getOtherEntities(player, target.boundingBox.expand(24.0)).forEach {
            if (it is LivingEntity) {
                it.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, 200, 0, true, false))
            }
        }
    }
}