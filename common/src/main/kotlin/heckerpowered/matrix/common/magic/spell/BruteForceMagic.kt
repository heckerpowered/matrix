/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.world.effect.MobEffectInstance

object BruteForceMagic : Magic(
    MagicDefinition(
        Matrix.identifier("brute_force"),
        45.mana,
        40.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)
        val target = invocation.target
        target.addEffect(MobEffectInstance(ModMobEffects.Exposed, 200, 0, false, true))
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailability {
        val availability = super.availableStatus(context)

        val target = context.target
        if (target?.isInvulnerableToEffect(ModMobEffects.Exposed) == true) {
            availability += MagicAvailableStatus.TargetImmune
        }
        return availability
    }
}