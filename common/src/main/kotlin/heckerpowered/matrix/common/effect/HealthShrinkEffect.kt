/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.entity.rule.AttributeComputationContext
import heckerpowered.matrix.common.entity.rule.AttributeComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.ai.attributes.Attributes

object HealthShrinkEffect : MobEffect(
    MobEffectCategory.HARMFUL,
    0x8B0000
), AttributeComputationRule {
    init {
        RuleRegistry.register<AttributeComputationRule>(this)
    }

    override fun onComputation(context: AttributeComputationContext) {
        val entity = context.entity
        val effect = entity.getEffect(ModMobEffects.HealthShrink) ?: return
        val attribute = context.attribute
        if (attribute == Attributes.MAX_HEALTH) {
            context.multiplier -= (effect.amplifier * 0.025F).coerceAtMost(0.5F)
        }
    }
}