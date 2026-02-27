/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.proximatePropagation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.CostCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.lerp
import net.minecraft.entity.player.PlayerEntity

object ProximatePropagationEnchantment : MagicCalculationContributor {
    fun onInitialize() {
        RuleRegistry.register<MagicCalculationContributor>(this)
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is CostCalculationSink) return
        val caster = context.caster?.entityOrNull() as? PlayerEntity ?: return
        val target = context.target ?: return
        if (caster.wizardHelmet.getEnchantmentLevel(proximatePropagation) <= 0) return

        val squaredDistance = caster.squaredDistanceTo(target)
        val maxDistanceSquare = 12.0 * 12.0
        val minDistanceSquare = 4.0 * 4.0
        val lerpFactor = 1 - squaredDistance.inverseLerp(minDistanceSquare..maxDistanceSquare).coerceIn(0.0, 1.0)
        sink.costReduction += lerpFactor.lerp(0.0..0.35)
    }
}