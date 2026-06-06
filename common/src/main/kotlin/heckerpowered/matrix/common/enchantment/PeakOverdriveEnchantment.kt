/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.enchantment.ModEnchantments.PeakOverdrive
import heckerpowered.matrix.common.item.ModComponents
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.contributor.MagicCalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.BloodPactCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.ChannelTimeCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.MagicCalculationSink
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.world.entity.player.Player

object PeakOverdriveEnchantment : MagicCalculationContributor, CalculationContributor, ChannelEffect, DamageComputationRule {
    fun onInitialize() {
        RuleRegistry.register<DamageComputationRule>(this)
        RuleRegistry.register<MagicCalculationContributor>(this)
        RuleRegistry.register<CalculationContributor>(this)
        RuleRegistry.register<ChannelEffect>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (attacker !is Player || !attacker.isBloodPactActive) return
        val enchantmentLevel = attacker.getEnchantmentLevel(PeakOverdrive)
        if (enchantmentLevel <= 0) return

        context.damageMultiplier += 0.5
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        val caster = context.playerOrNull() ?: return
        if (!caster.isBloodPactActive) return
        if (caster.getEnchantmentLevel(PeakOverdrive) <= 0) return
        if (sink !is ChannelTimeCalculationSink) return

        sink.channelSpeedBonus += 0.5
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.entityOrNull() as Player
        if (!caster.isBloodPactActive) return
        if (caster.getEnchantmentLevel(PeakOverdrive) <= 0) return

        val currentLoad = caster.wizardHelmetStack.getOrDefault(ModComponents.load, .0)
        caster.wizardHelmetStack.set(ModComponents.load, currentLoad + 1)
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        val caster = context.playerOrNull() ?: return
        if (!caster.isBloodPactActive) return
        if (caster.getEnchantmentLevel(PeakOverdrive) <= 0) return
        if (sink !is BloodPactCalculationSink) return

        sink.exchangeRate += 1.0
    }
}
