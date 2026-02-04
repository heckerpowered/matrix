/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.BLOOD_PACT_EFFECT
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.CastingResource
import heckerpowered.matrix.common.magic.resource.CastingResourceContributor
import heckerpowered.matrix.common.magic.resource.CastingResourceRegistry
import heckerpowered.matrix.common.magic.resource.HealthReserve
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult

val PlayerEntity.isBloodPactActive: Boolean
    get() = hasStatusEffect(BLOOD_PACT_EFFECT)

object BloodPactEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0xFF0000
), CastingResourceContributor {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
        CastingResourceRegistry.register(this)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        if (attacker.hasStatusEffect(BLOOD_PACT_EFFECT) &&
            accumulator.damageSource.isOf(MatrixDamageTypes.magic)
        ) {
            accumulator.damageMultiplier += 0.1
        }

        return ActionResult.PASS
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
}