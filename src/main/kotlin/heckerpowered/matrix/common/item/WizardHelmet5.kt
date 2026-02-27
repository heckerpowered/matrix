/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelTimeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

/**
 * Wizard Helmet 5 'Axiom of Annihilation'
 */
object WizardHelmet5 : WizardHelmet(
    12.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
        .component(MatrixComponents.MAX_LOAD, 20.0)
), MagicCalculationContributor, DamageComputationRule {
    init {
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    private fun onLivingAttack(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker!!
        if (attacker !is ServerPlayerEntity) {
            return ActionResult.PASS
        }
        if (!event.damageSource.isOf(MatrixDamageTypes.magic)) {
            return ActionResult.PASS
        }

        if (attacker.wizardHelmet.item is WizardHelmet5) {
            event.damageMultiplier += 1
        }
        return ActionResult.PASS
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        if (sink !is ChannelTimeCalculationSink) return
        val player = context.playerOrNull() ?: return
        if (player.wizardHelmet.item is WizardHelmet5) {
            sink.channelSpeedBonus += 1.0
        }
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? ServerPlayerEntity ?: return
        if (!context.source.isOf(MatrixDamageTypes.magic)) return

        if (attacker.wizardHelmet.item is WizardHelmet5) {
            context.damageMultiplier += 1
        }
    }
}