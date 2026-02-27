/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Rarity

/**
 * Wizard Helmet 3 'Blood-forged Ruin'
 */
object WizardHelmet3 : WizardHelmet(
    10.0,
    Settings()
        .fireproof()
        .rarity(Rarity.UNCOMMON)
        .component(MatrixComponents.MAX_LOAD, 15.0)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        if (!context.source.isOf(MatrixDamageTypes.magic)) return
        val attacker = context.attacker as? ServerPlayerEntity ?: return
        if (attacker.wizardHelmet.item !is WizardHelmet3) return

        context.damageMultiplier += 1
    }
}