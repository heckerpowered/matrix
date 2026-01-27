/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.HEALTH_SHRINK_EFFECT
import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.core.Accumulator
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.registry.entry.RegistryEntry

object HealthShrinkEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0x8B0000
) {
    init {
        AccumulateAttributeValueCallback.EVENT.register(::getAttributeValue)
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        val effect = entity.getStatusEffect(HEALTH_SHRINK_EFFECT) ?: return
        if (attribute == EntityAttributes.GENERIC_MAX_HEALTH) {
            accumulator.multiplier -= (effect.amplifier * 0.025F).coerceAtMost(0.5F)
        }
    }
}