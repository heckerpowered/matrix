/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity

object WitherArmorEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0x5A5A5A
) {
    override fun applyInstantaneousEffect(level: ServerLevel, source: Entity?, owner: Entity?, mob: LivingEntity, amplification: Int, scale: Double) {
        super.applyInstantaneousEffect(level, source, owner, mob, amplification, scale)

        // Ensure the new absorption amount will not exceed the maximum health of the entity
        // and not less than the current absorption amount. Do not use .coerceIn(), the size
        // relationship between the two values is unknown.
        val maxAbsorptionAmount = mob.maxHealth
        val newAbsorptionAmount = (mob.absorptionAmount + mob.maxHealth * (0.05F + (amplification + 1) * 0.05F))
            .coerceAtMost(maxAbsorptionAmount)
            .coerceAtLeast(mob.absorptionAmount)

        mob.internalSetAbsorptionAmount(newAbsorptionAmount)
    }
}