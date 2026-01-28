/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.EXPOSED_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.effect.StatusEffectInstance

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
        target.addStatusEffect(StatusEffectInstance(EXPOSED_EFFECT, 200, 0, false, true))
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        if (target?.isInvulnerableToEffect(EXPOSED_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }
        return super.availableStatus(context)
    }
}