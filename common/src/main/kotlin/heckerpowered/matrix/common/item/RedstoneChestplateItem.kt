/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.combat.damage.DamageSettlementContext
import heckerpowered.matrix.common.combat.damage.DamageSettlementRule
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitPower
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer
import kotlin.math.ceil

object RedstoneChestplateItem : Item(
    Properties().setId(ModItemIds.redstoneChestplate)
        .humanoidArmor(ModArmorMaterials.redstone, ArmorType.CHESTPLATE)
        .component(redstoneSuitMaxPower, 20)
        .component(redstoneSuitPower, 0)
), RedstoneSuit, TooltipProvider, DamageSettlementRule {
    init {
        RuleRegistry.register<DamageSettlementRule>(this)
    }

    override fun onSettlement(context: DamageSettlementContext) {
        if (context.remainingDamage <= 0f) return

        val target = context.target
        val chestplate = target.getItemBySlot(EquipmentSlot.CHEST).takeIf { it.isRedstoneSuit() } ?: return
        val power = chestplate.redstoneSuitPower.takeIf { it > 0 } ?: return

        val maxAbsorbByRatio = context.realizedDamage * 0.4f
        val maxAbsorbByPower = power * 4f

        val absorbCapacity = minOf(maxAbsorbByRatio, maxAbsorbByPower)
        val absorbedDamage = context.consume(absorbCapacity).takeIf { it > 0 } ?: return

        val powerUsage = ceil(absorbedDamage / 4.0).toLong()
        chestplate.redstoneSuitPower = (power - powerUsage).coerceAtLeast(0)
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        RedstoneSuit.appendTooltip(components, consumer)
        consumer.accept(
            MatrixLanguage.redstoneChestplateDescription.copy()
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
    }
}
