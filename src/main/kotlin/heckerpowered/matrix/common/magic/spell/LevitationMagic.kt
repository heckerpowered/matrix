/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.core.*
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects

object LevitationMagic : Magic(
    MagicDefinition(
        Matrix.identifier("levitation"),
        30.mana,
        40.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val target = invocation.target
        val statusEffectInstance = target.getStatusEffect(StatusEffects.LEVITATION)
        val amplifier = statusEffectInstance?.amplifier ?: -1
        target.addStatusEffect(StatusEffectInstance(StatusEffects.LEVITATION, 20 * 10, amplifier + 1))
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailableStatus {
        val target = context.target

        if (target?.isInvulnerableToEffect(StatusEffects.LEVITATION) == true) {
            return MagicAvailableStatus.TARGET_IMMUNE
        }

        return super.availableStatus(context)
    }
}