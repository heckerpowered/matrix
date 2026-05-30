/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attacker
import heckerpowered.matrix.common.effect.BloodPactEffect
import heckerpowered.matrix.common.effect.isBloodPactActive
import heckerpowered.matrix.common.entity.rule.AttributeComputationContext
import heckerpowered.matrix.common.entity.rule.AttributeComputationRule
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.calculation.contributor.CalculationContributor
import heckerpowered.matrix.common.magic.rule.calculation.sink.BloodPactCalculationSink
import heckerpowered.matrix.common.magic.rule.calculation.sink.CalculationSink
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.healOverflow
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import java.util.function.Consumer
import kotlin.math.abs
import kotlin.math.floor

/**
 * Wizard Helmet 13 'Overflux Crown'
 */
object WizardHelmet13 : WizardHelmet(
    Properties()
        .fireResistant()
        .rarity(Rarity.EPIC)
        .maxMana(12.0)
        .maxLoad(100.0)
        .component(ModComponents.accumulatedManaDelta, 0.0)
), CalculationContributor, AttributeComputationRule, DamageComputationRule {
    init {
        RuleRegistry.register<CalculationContributor>(this)
        RuleRegistry.register<AttributeComputationRule>(this)
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: AttributeComputationContext) {
        val attribute = context.attribute
        val entity = context.entity
        if (attribute != Attributes.MAX_HEALTH &&
            attribute != Attributes.ATTACK_DAMAGE &&
            attribute != Attributes.ARMOR &&
            attribute != Attributes.ARMOR_TOUGHNESS
        ) return
        if (entity !is Player) return

        // Player.inventory is normally non-null, but this callback can be invoked from
        // the Player constructor before the inventory field has been initialized.
        val wizardHelmet = runCatching { entity.wizardHelmetStack }.getOrNull() ?: return
        val item = wizardHelmet.item
        if (!entity.isBloodPactActive || item !is WizardHelmet13) return

        val calculationContext = MagicCalculationContext.fromEntity(entity, null)
        val excessConversionEfficiency = item.getExcessExchangeRate(calculationContext)
        if (attribute == Attributes.ATTACK_DAMAGE) {
            context.baseValue += excessConversionEfficiency * 3
        } else {
            context.multiplier += excessConversionEfficiency * 0.25
        }
    }

    override fun onManaChanged(player: Player, previousMana: Double, currentMana: Double) {
        super.onManaChanged(player, previousMana, currentMana)
        if (player.level().isClientSide) return

        val wizardHelmet = player.wizardHelmetStack
        if (wizardHelmet.item !is WizardHelmet13) return
        if (!player.isBloodPactActive) return

        val accumulatedManaDelta = wizardHelmet[ModComponents.accumulatedManaDelta] ?: .0
        wizardHelmet[ModComponents.accumulatedManaDelta] = accumulatedManaDelta + abs(currentMana - previousMana)
    }

    override fun onBloodPactActive(player: ServerPlayer, itemStack: ItemStack) {
        super.onBloodPactActive(player, itemStack)
        itemStack[ModComponents.accumulatedManaDelta] = 0.0
        player.healOverflow(player.maxMana.toDouble().toFloat())
    }

    fun getExcessExchangeRate(context: MagicCalculationContext): Double {
        val exchangeRate = BloodPactEffect.getExchangeRate(context)
        return exchangeRate - 2.0
    }

    override fun inventoryTick(itemStack: ItemStack, level: ServerLevel, owner: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(itemStack, level, owner, slot)
        if (owner !is Player || !owner.isBloodPactActive) return

        val calculationContext = MagicCalculationContext.fromEntity(owner, null)
        val exchangeRate = BloodPactEffect.getExchangeRate(calculationContext)
        itemStack[ModComponents.bloodPactExchangeRate] = exchangeRate
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        super.addToTooltip(context, consumer, flag, components)

        val exchangeRate = components[ModComponents.bloodPactExchangeRate] ?: BloodPactEffect.DEFAULT_EXCHANGE_RATE
        val accumulatedManaDelta = components[ModComponents.accumulatedManaDelta] ?: .0
        consumer.accept(MatrixLanguage.wizardHelmetBloodPactExchangeRate.copy().append("${exchangeRate * 100}%"))
        consumer.accept(MatrixLanguage.wizardHelmetManaDeltaDescription.copy().append("${floor(accumulatedManaDelta).toLong()}"))
    }

    override fun contribute(context: MagicCalculationContext, sink: CalculationSink) {
        if (sink !is BloodPactCalculationSink) return
        val caster = context.playerOrNull() ?: return
        if (caster.wizardHelmetStack.item !is WizardHelmet13) return
        if (!caster.isBloodPactActive) return

        val accumulatedManaDelta = caster.wizardHelmetStack[ModComponents.accumulatedManaDelta] ?: .0
        val bonusExchangeRate = 1.0 + (accumulatedManaDelta * 0.02).coerceAtMost(1.0)
        sink.exchangeRate += bonusExchangeRate
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attacker as? Player ?: return
        val wizardHelmet = attacker.wizardHelmetStack
        val item = wizardHelmet.item
        if (!attacker.isBloodPactActive || item !is WizardHelmet13) return

        val calculationContext = MagicCalculationContext.fromEntity(attacker, context.target)
        val excessExchangeRate = item.getExcessExchangeRate(calculationContext)
        context.damageMultiplier += excessExchangeRate * 0.25
    }
}