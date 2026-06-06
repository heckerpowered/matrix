/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.core.extension.addAbsorptionUpTo
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

object WitherArmorEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0x5A5A5A
) {
    override fun onEffectStarted(mob: LivingEntity, amplifier: Int) {
        super.onEffectStarted(mob, amplifier)

        mob.addAbsorptionUpTo(mob.maxHealth * (0.05F + (amplifier + 1) * 0.05F), mob.maxHealth)
    }
}