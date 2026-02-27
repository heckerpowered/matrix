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
 * Wizard Helmet 4 'Might and Method'
 */
object WizardHelmet4 : WizardHelmet(
    11.0,
    Settings()
        .fireproof()
        .rarity(Rarity.RARE)
        .component(MatrixComponents.MAX_LOAD, 20.0)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? ServerPlayerEntity ?: return
        if (!context.source.isOf(MatrixDamageTypes.magic)) return
        if (attacker.wizardHelmet.item !is WizardHelmet4) return

        context.damageMultiplier += 0.85
        if ((1..100).random() <= 35) {
            context.damageMultiplier += 1.0
            attacker.addCritParticles(context.target)
            attacker.addEnchantedHitParticles(context.target)
        }
    }
}