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
 * Wizard Helmet 4 'Might and Method'
 */
object WizardHelmet4 : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmet4)
        .rarity(Rarity.RARE)
        .maxMana(11.0)
        .maxLoad(120.0)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? ServerPlayer ?: return
        if (!context.source.`is`(MatrixDamageTypes.magic)) return
        if (attacker.wizardHelmetStack.item !is WizardHelmet4) return

        context.damageMultiplier += 0.85
        if ((1..100).random() <= 35) {
            context.damageMultiplier += 1.0
            attacker.crit(context.target)
            attacker.magicCrit(context.target)
        }
    }
}