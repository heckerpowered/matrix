/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.effect.MatrixStatusEffects.BLOOD_PACT_EFFECT
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.CastingResourceContributor
import heckerpowered.matrix.common.magic.resource.HealthReserve
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.CalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.BloodPactCalculationSink
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity

val PlayerEntity.isBloodPactActive: Boolean
    get() = hasStatusEffect(BLOOD_PACT_EFFECT)

object BloodPactEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF0000
), CastingResourceContributor, DamageComputationRule {
    const val DEFAULT_BLOOD_PACT_CONVERT_RATIO = 2.0

    init {
        RuleRegistry.register<CastingResourceContributor>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        val source = context.source
        if (!attacker.hasStatusEffect(BLOOD_PACT_EFFECT)) return
        if (source.isOf(MatrixDamageTypes.magic)) return

        context.damageMultiplier += 0.1
    }

    /**
     * Returns resources that are available under the given context.
     *
     * The returned list may be empty.
     */
    override fun contribute(context: MagicCalculationContext, sink: MutableCollection<CastingResource>) {
        val caster = context.caster?.entityOrNull() as? PlayerEntity ?: return
        if (!caster.isBloodPactActive) return
        sink += HealthReserve()
    }

    fun getBloodPactConversionRatio(context: MagicCalculationContext): Double {
        val sink = BloodPactCalculationSink()
        CalculationPipeline.apply(context, sink)
        return sink.conversionRatio
    }
}