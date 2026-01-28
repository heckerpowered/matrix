/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ARMOR_PENETRATION_EFFECT
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.effect.StatusEffectInstance

object ArmorPenetrationMagic : Magic(
    MagicDefinition(
        Matrix.identifier("armor_penetration"),
        30.mana,
        60.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val effect = StatusEffectInstance(ARMOR_PENETRATION_EFFECT, 200, 0, false, false)
        target.addStatusEffect(effect)
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target
        if (target?.isInvulnerableToEffect(ARMOR_PENETRATION_EFFECT) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }
}