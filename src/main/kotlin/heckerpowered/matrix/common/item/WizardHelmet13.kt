/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.client.player
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.ChannelQueue.Companion.getChannelQueue
import heckerpowered.matrix.common.magic.core.ExecutionPayload
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.persistent.wizardHelmet
import heckerpowered.matrix.core.Accumulator
import heckerpowered.matrix.core.extensions.LivingEntityExtensions.healOverflow
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity
import kotlin.math.abs
import kotlin.math.floor

/**
 * Wizard Helmet 13 'Overflux Crown'
 */
object WizardHelmet13 : WizardHelmet(
    12.0,
    Settings()
        .fireproof()
        .rarity(Rarity.EPIC)
        .component(MatrixComponents.MAX_LOAD, 20.0)
        .component(MatrixComponents.ACCUMULATED_MANA_DELTA, 0.0)
) {
    init {
        LivingHurtCallback.EVENT.register(this::onLivingHurt)
        AccumulateAttributeValueCallback.EVENT.register(this::getAttributeValue)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        val attacker = event.attacker
        if (attacker !is PlayerEntity) {
            return ActionResult.PASS
        }
        val wizardHelmet = attacker.wizardHelmet
        val item = wizardHelmet.item
        if (!attacker.isBloodPactActive || item !is WizardHelmet13) {
            return ActionResult.PASS
        }

        val excessConversionEfficiency = item.getExcessConversionEfficiency(attacker, event.target, attacker.getChannelQueue(event.target))
        event.damageMultiplier += excessConversionEfficiency * 0.25
        return ActionResult.PASS
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute != EntityAttributes.GENERIC_MAX_HEALTH &&
            attribute != EntityAttributes.GENERIC_ATTACK_DAMAGE &&
            attribute != EntityAttributes.GENERIC_ARMOR &&
            attribute != EntityAttributes.GENERIC_ARMOR_TOUGHNESS
        ) {
            return
        }
        if (entity !is PlayerEntity || entity.inventory == null) {
            return
        }
        val wizardHelmet = entity.wizardHelmet
        val item = wizardHelmet.item
        if (!entity.isBloodPactActive || item !is WizardHelmet13) {
            return
        }

        val excessConversionEfficiency = item.getExcessConversionEfficiency(entity, null, null)
        if (attribute == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
            accumulator.baseBonus += excessConversionEfficiency * 3
        } else {
            accumulator.multiplier += excessConversionEfficiency * 0.25
        }
    }

    override fun getBloodPactConversionEfficiency(player: PlayerEntity, target: LivingEntity?, queue: ChannelQueue?, data: ExecutionPayload): Double {
        val conversionEfficiency = super.getBloodPactConversionEfficiency(player, target, queue, data)
        if (player.isBloodPactActive) {
            return conversionEfficiency + 1.0 +
                    (player.wizardHelmet.getOrDefault(MatrixComponents.ACCUMULATED_MANA_DELTA, .0) * 0.01).coerceAtMost(1.0)
        }
        return conversionEfficiency
    }

    override fun onManaChanged(player: PlayerEntity, previousMana: Double, currentMana: Double) {
        super.onManaChanged(player, previousMana, currentMana)

        val wizardHelmet = player.wizardHelmet
        if (wizardHelmet.item is WizardHelmet13) {
            val accumulatedManaDelta = wizardHelmet.getOrDefault(MatrixComponents.ACCUMULATED_MANA_DELTA, .0)
            wizardHelmet.set(MatrixComponents.ACCUMULATED_MANA_DELTA, accumulatedManaDelta + abs(currentMana - previousMana))
        }
    }

    override fun onBloodPactActive(player: ServerPlayerEntity, itemStack: ItemStack) {
        super.onBloodPactActive(player, itemStack)
        itemStack.set(MatrixComponents.ACCUMULATED_MANA_DELTA, 0.0)
        player.healOverflow(player.maxMana.amount.toFloat())
    }

    fun getExcessConversionEfficiency(player: PlayerEntity, target: LivingEntity?, queue: ChannelQueue?, data: ExecutionPayload = ExecutionPayload()): Double {
        val conversionEfficiency = getBloodPactConversionEfficiency(player, target, queue, data)
        return conversionEfficiency - 2.0
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)

        val conversionEfficiency = getBloodPactConversionEfficiency(player, null, null)
        val accumulatedManaDelta = stack.getOrDefault(MatrixComponents.ACCUMULATED_MANA_DELTA, .0)
        tooltip.add(MatrixLanguage.wizardHelmetBloodPactConversionEfficiency.copy().append("${conversionEfficiency * 100}%"))
        tooltip.add(MatrixLanguage.wizardHelmetManaDeltaDescription.copy().append("${floor(accumulatedManaDelta).toLong()}"))
    }
}