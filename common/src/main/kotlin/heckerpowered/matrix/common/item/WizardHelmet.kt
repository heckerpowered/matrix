/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.ledger.transaction.constraint.BoundedTransactionConstraint
import heckerpowered.matrix.common.enchantment.ModEnchantments.enchantmentKey
import heckerpowered.matrix.common.entity.rule.EquipItemContext
import heckerpowered.matrix.common.entity.rule.EquipItemRule
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.CalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.MaxManaCalculationSink
import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.common.magic.system.ManaLedger
import heckerpowered.matrix.common.magic.system.ManaLedger.toLedgerUnits
import heckerpowered.matrix.common.persistent.wizardHelmetStack
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrNull
import kotlin.math.exp
import kotlin.math.ln
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

open class WizardHelmet(properties: Properties) : Item(
    properties
        .humanoidArmor(ModArmorMaterials.wizard, ArmorType.HELMET)
        .fireResistant()
), TooltipProvider {
    companion object : EquipItemRule {
        init {
            RuleRegistry.register<EquipItemRule>(this)
        }

        override fun onEquipItem(context: EquipItemContext) {
            val entity = context.entity as? ServerPlayer ?: return
            val stack = context.stack

            val item = stack.item
            val maxMana = if (item is WizardHelmet) item.getMaxMana(entity, stack).mana else .0.mana
            val account = ManaLedger.account(entity)
            account.transactionConstraints = setOf(BoundedTransactionConstraint(0, maxMana.toLedgerUnits()))
        }
    }

    open fun getMagics(player: Player, itemStack: ItemStack): Sequence<Magic> {
        if (itemStack.isEmpty) {
            return emptySequence()
        }

        val enchantmentRegistry = player.registryAccess()[Registries.ENCHANTMENT].getOrNull()?.value() ?: return emptySequence()
        val enchantments = itemStack.enchantments.entrySet()
            .asSequence()
            .map { it.key.value() }
            .mapNotNull { enchantmentRegistry.getKey(it) }
            .mapNotNull { Magics[it] }
        return enchantments
    }

    open fun hasMagic(player: Player, itemStack: ItemStack, magic: Magic): Boolean {
        val enchantmentRegistry = player.registryAccess()[Registries.ENCHANTMENT].getOrNull()?.value() ?: return false
        val enchantmentHolder = enchantmentRegistry.get(magic.enchantmentKey).getOrNull() ?: return false
        return itemStack.enchantments.getLevel(enchantmentHolder) > 0
    }

    open fun onManaChanged(player: Player, previousMana: Double, currentMana: Double) {
    }

    open fun onBloodPactActive(player: ServerPlayer, itemStack: ItemStack) {
    }

    open fun getMaxMana(player: Player, itemStack: ItemStack): Double {
        val defaultMaxMana = itemStack[ModComponents.maxMana] ?: .0

        val context = MagicCalculationContext(CasterContext.fromEntity(player))
        val sink = MaxManaCalculationSink(maxMana = defaultMaxMana)
        CalculationPipeline.apply(context, sink)

        val maxMana = sink.maxMana
        val multiplier = sink.multiplier
        return maxMana * multiplier
    }

    protected open fun overloadBreakChancePerSecond(extraLoad: Double): Double {
        if (extraLoad <= 20.0) {
            return 0.0
        }

        val targetChance = 0.8
        val secondsPerDay = 1.days.toDouble(DurationUnit.SECONDS)
        val secondsPerHour = 1.hours.toDouble(DurationUnit.SECONDS)
        val exponent = ln(secondsPerDay / secondsPerHour) / (40.0 - 30.0)
        val referenceFailureRate = -ln(1.0 - targetChance) / secondsPerDay
        val failureRatePerSecond = referenceFailureRate * exp(exponent * (extraLoad - 30.0))
        return 1.0 - exp(-failureRatePerSecond)
    }

    override fun inventoryTick(itemStack: ItemStack, level: ServerLevel, owner: Entity, slot: EquipmentSlot?) {
        super.inventoryTick(itemStack, level, owner, slot)
        if (owner.tickCount % 20 != 0) return

        val currentLoad = itemStack[ModComponents.load]?.takeIf { it > 0 } ?: return
        val maxLoad = itemStack[ModComponents.maxLoad] ?: 0.0

        val nextLoad = currentLoad - 0.1
        itemStack[ModComponents.load] = nextLoad.coerceAtLeast(.0)

        val extraLoad = (currentLoad - maxLoad).takeIf { it > 0 } ?: return
        if (Random.nextDouble() < overloadBreakChancePerSecond(extraLoad)) {
            itemStack.shrink(1)
            return
        }
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        val currentLoad = components[ModComponents.load] ?: .0
        val maxLoad = components[ModComponents.maxLoad] ?: .0
        if (maxLoad <= 0 || currentLoad < 0) {
            return
        }

        val load = ((currentLoad / maxLoad) * 10000).toLong() / 100.0
        consumer.accept(MatrixLanguage.wizardHelmetLoadDescription.copy().append("$load%"))
    }
}

fun Player.getMagics(): Sequence<Magic> {
    val helmet = wizardHelmetStack
    val item = helmet.item
    if (item !is WizardHelmet) return emptySequence()

    return item.getMagics(this@getMagics, helmet)
}

fun Player.hasMagic(magic: Magic): Boolean {
    val helmet = wizardHelmetStack
    val item = helmet.item
    if (item !is WizardHelmet) return false

    return item.hasMagic(this, helmet, magic)
}

fun Item.Properties.maxMana(value: Double): Item.Properties {
    component(ModComponents.maxMana, value)
    return this
}

fun Item.Properties.maxLoad(value: Double): Item.Properties {
    component(ModComponents.maxLoad, value)
    return this
}