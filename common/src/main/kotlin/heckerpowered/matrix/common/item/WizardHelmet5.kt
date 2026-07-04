/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelTimeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Rarity

/**
 * Wizard Helmet 5 'Axiom of Annihilation'
 */
object WizardHelmet5 : WizardHelmet(
    Properties().setId(ModItemIds.wizardHelmet5)
        .rarity(Rarity.EPIC)
        .maxMana(12.0)
        .maxLoad(125.0)
), MagicCalculationContributor, DamageComputationRule {
    init {
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is ChannelTimeCalculationSink) return
        val player = context.playerOrNull() ?: return
        if (player.wizardHelmetStack.item is WizardHelmet5) {
            sink.channelSpeedBonus += 1.0
        }
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? ServerPlayer ?: return
        if (!context.source.`is`(MatrixDamageTypes.magic)) return

        if (attacker.wizardHelmetStack.item is WizardHelmet5) {
            context.damageMultiplier += 1
        }
    }
}