/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Rarity

/**
 * Wizard Helmet 3 'Blood-forged Ruin'
 */
object WizardHelmet3 : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmet3)
        .rarity(Rarity.UNCOMMON)
        .maxMana(10.0)
        .maxLoad(200.0)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        if (!context.source.`is`(MatrixDamageTypes.magic)) return
        
        val attacker = context.attacker as? ServerPlayer ?: return
        if (attacker.wizardHelmetStack.item !is WizardHelmet3) return

        context.damageMultiplier += 1
    }
}