/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
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
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.player.Player

val Player.isBloodPactActive: Boolean
    get() = hasEffect(ModMobEffects.BloodPact)

object BloodPactEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0xFF0000
), CastingResourceContributor, DamageComputationRule {
    const val DEFAULT_EXCHANGE_RATE = 2.0

    init {
        RuleRegistry.register<CastingResourceContributor>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        val source = context.source
        if (!attacker.hasEffect(ModMobEffects.BloodPact)) return
        if (source.`is`(MatrixDamageTypes.magic)) return

        context.damageMultiplier += 0.1
    }

    /**
     * Returns resources that are available under the given context.
     *
     * The returned list may be empty.
     */
    override fun contribute(context: MagicCalculationContext, sink: MutableCollection<CastingResource>) {
        val caster = context.caster?.entityOrNull() as? Player ?: return
        if (!caster.isBloodPactActive) return
        sink += HealthReserve()
    }

    fun getExchangeRate(context: MagicCalculationContext): Double {
        val sink = BloodPactCalculationSink()
        CalculationPipeline.apply(context, sink)
        return sink.exchangeRate
    }
}