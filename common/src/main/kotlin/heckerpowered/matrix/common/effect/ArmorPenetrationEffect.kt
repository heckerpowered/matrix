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

object ArmorPenetrationEffect : MobEffect(
    MobEffectCategory.HARMFUL,
    0xFF4500
), AttributeComputationRule {
    init {
        RuleRegistry.register<AttributeComputationRule>(this)
    }

    override fun onComputation(context: AttributeComputationContext) {
        val entity = context.entity
        val attribute = context.attribute

        if (attribute != Attributes.ARMOR_TOUGHNESS) return

        val armorPenetrationInstance = entity.getEffect(ModMobEffects.ArmorPenetration) ?: return
        val amplifier = armorPenetrationInstance.amplifier + 1
        context.multiplier -= amplifier * 0.4
    }
}