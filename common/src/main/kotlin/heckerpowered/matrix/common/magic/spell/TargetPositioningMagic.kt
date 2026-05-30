/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.core.utility.getOtherEntities
import heckerpowered.matrix.core.utility.withinDistance
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity

object TargetPositioningMagic : Magic(
    MagicDefinition(
        Matrix.identifier("target_positioning"),
        7.mana,
        20.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull()
        val target = invocation.target
        target.getOtherEntities(24.0)
            .withinDistance(target, 24.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != caster }
            .forEach {
                it.addEffect(MobEffectInstance(MobEffects.GLOWING, 200, 0, true, false))
            }
    }
}