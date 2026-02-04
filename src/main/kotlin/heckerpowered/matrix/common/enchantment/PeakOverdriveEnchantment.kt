/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.PEAK_OVERDRIVE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.getEnchantmentLevel
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.item.MatrixComponents
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
import heckerpowered.matrix.common.magic.rule.registry.MagicRuleRegistry
import heckerpowered.matrix.common.persistent.wizardHelmet
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object PeakOverdriveEnchantment : MagicCalculationContributor, CalculationContributor, ChannelEffect {
    fun onInitialize() {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
        MagicRuleRegistry.register(this)
    }

    private fun onLivingAttack(accumulator: DamageAccumulator): ActionResult {
        val attacker = accumulator.attacker!!
        if (attacker !is PlayerEntity || !attacker.isBloodPactActive) {
            return ActionResult.PASS
        }

        val equippedHelmet = attacker.wizardHelmet
        if (equippedHelmet.isEmpty) {
            return ActionResult.PASS
        }

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(PEAK_OVERDRIVE_ENCHANTMENT_KEY)
        val enchantmentLevel = EnchantmentHelper.getLevel(enchantmentEntry, equippedHelmet)
        if (enchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        accumulator.damageMultiplier += 0.5
        return ActionResult.PASS
    }

    override fun contribute(magic: Magic, context: MagicCalculationContext, sink: MagicCalculationSink) {
        val player = context.playerOrNull() ?: return
        if (!player.isBloodPactActive) return
        if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) <= 0) return
        if (sink !is ChannelTimeCalculationSink) return

        sink.channelSpeedBonus += 0.5
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.entityOrNull() as PlayerEntity
        if (!caster.isBloodPactActive) return
        if (caster.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) <= 0) return

        val currentLoad = caster.wizardHelmet.getOrDefault(MatrixComponents.LOAD, .0)
        caster.wizardHelmet.set(MatrixComponents.LOAD, currentLoad + 1)
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        val player = context.playerOrNull() ?: return
        if (!player.isBloodPactActive) return
        if (player.wizardHelmet.getEnchantmentLevel(PEAK_OVERDRIVE_ENCHANTMENT_KEY) <= 0) return
        if (sink !is BloodPactCalculationSink) return

        sink.conversionRatio += 1.0
    }
}