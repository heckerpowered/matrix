/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.client.player
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory

object WitherArmorEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x5A5A5A
) {
    override fun onApplied(entity: LivingEntity, amplifier: Int) {
        super.onApplied(entity, amplifier)
        if (entity.world.isClient) {
            return
        }

        // Ensure the new absorption amount will not exceed the maximum health of the entity
        // and not less than the current absorption amount. Do not use .coerceIn(), the size
        // relationship between the two values is unknown.
        val maxAbsorptionAmount = entity.maxHealth
        val newAbsorptionAmount = (entity.absorptionAmount + entity.maxHealth * (0.05F + (amplifier + 1) * 0.05F))
            .coerceAtMost(maxAbsorptionAmount)
            .coerceAtLeast(player.absorptionAmount)

        entity.setAbsorptionAmountUnclamped(newAbsorptionAmount)
    }
}