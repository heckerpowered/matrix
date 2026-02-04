/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.enchantmentKey
import heckerpowered.matrix.common.event.ItemStackEquippedCallback
import heckerpowered.matrix.common.item.MatrixComponents.MAX_MANA
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.rule.calculation.pipeline.CalculationPipeline
import heckerpowered.matrix.common.magic.rule.calculation.sink.MaxManaCalculationSink
import heckerpowered.matrix.common.magic.system.MagicManager
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.component.DataComponentTypes
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.RegistryKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.world.World
import kotlin.random.Random

open class WizardHelmet(maxMana: Double, settings: Settings) : ArmorItem(
    wizardArmorMaterial,
    Type.HELMET,
    settings
        .component(MAX_MANA, maxMana)
        .component(DataComponentTypes.MAX_STACK_SIZE, 1)
        .fireproof()
) {
    companion object {
        private val allMagicsByEnchantment by lazy {
            val map = mutableMapOf<RegistryKey<Enchantment>, Magic>()
            for (magic in MagicManager.getRegisteredMagics()) {
                map[magic.enchantmentKey] = magic
            }
            map
        }

        init {
            ItemStackEquippedCallback.EVENT.register(::onItemStackEquipped)
        }

        private fun onItemStackEquipped(entity: LivingEntity, equipmentSlot: EquipmentSlot, previousItemStack: ItemStack, currentItemStack: ItemStack) {
            val item = currentItemStack.item
            if (entity !is ServerPlayerEntity) {
                return
            }

            if (item is WizardHelmet) {
                entity.maxMana = item.getMaxMana(entity, currentItemStack).mana
            } else {
                entity.maxMana = .0.mana
            }
        }
    }

    open fun getMagics(player: PlayerEntity, itemStack: ItemStack): List<Magic> {
        if (itemStack.isEmpty) {
            return listOf()
        }

        return itemStack.enchantments.enchantments
            .asSequence()
            .map { it.key }
            .filter { it.isPresent }
            .map { it.get() }.mapNotNull { allMagicsByEnchantment[it] }
            .toList()
    }

    open fun hasMagic(itemStack: ItemStack, magic: Magic): Boolean {
        return itemStack.enchantments.enchantments
            .asSequence()
            .map { it.key }
            .filter { it.isPresent }
            .map { it.get() }
            .any { it == magic.enchantmentKey }
    }

    open fun onManaChanged(player: PlayerEntity, previousMana: Double, currentMana: Double) {
    }

    open fun onBloodPactActive(player: ServerPlayerEntity, itemStack: ItemStack) {
    }

    open fun getMaxMana(player: PlayerEntity, itemStack: ItemStack): Double {
        val defaultMaxMana = itemStack.getOrDefault(MAX_MANA, .0)

        val context = MagicCalculationContext(CasterContext.fromEntity(player))
        val sink = MaxManaCalculationSink(maxMana = defaultMaxMana)
        CalculationPipeline.apply(context, sink)

        val maxMana = sink.maxMana
        val multiplier = sink.multiplier
        return maxMana * multiplier
    }

    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        super.inventoryTick(stack, world, entity, slot, selected)
        if (world.isClient) {
            return
        }

        val currentLoad = stack.getOrDefault(MatrixComponents.LOAD, .0)
        if (currentLoad <= .0) {
            return
        }
        val maxLoad = stack.getOrDefault(MatrixComponents.MAX_LOAD, .0)
        val extraLoad = currentLoad - maxLoad
        if (extraLoad > 0) {
            val breakChance = Random.nextDouble(100.0)
            if (extraLoad > breakChance) {
                stack.decrement(1)
            }
        }

        // Reduce 0.1% load per second
        if (entity.age % 20 == 0) {
            val nextLoad = currentLoad - 0.1
            stack.set(MatrixComponents.LOAD, nextLoad.coerceAtLeast(.0))
        }
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)
        val currentLoad = stack.getOrDefault(MatrixComponents.LOAD, .0)
        val maxLoad = stack.getOrDefault(MatrixComponents.MAX_LOAD, .0)
        if (maxLoad <= 0 || currentLoad < 0) {
            return
        }

        val load = ((currentLoad / maxLoad) * 10000).toLong() / 100.0
        tooltip.add(MatrixLanguage.wizardHelmetLoadDescription.copy().append("$load%"))
    }
}