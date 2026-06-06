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
        val effect = MobEffectInstance(ModMobEffects.ArmorPenetration, 200, 0, false, false)
        target.addEffect(effect)
    }

    override fun availableStatus(context: MagicCalculationContext): MagicAvailability {
        val availability = super.availableStatus(context)

        val target = context.target
        if (target?.isInvulnerableToEffect(ModMobEffects.ArmorPenetration) == true) {
            availability.add(MagicAvailableStatus.TargetImmune)
        }

        return availability
    }
}