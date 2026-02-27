/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageRealizationContext
import heckerpowered.matrix.common.combat.damage.DamageRealizationRule
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object RedstoneChestplateItem : ArmorItem(
    redstoneArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(24))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit, DamageRealizationRule {
    init {
        ItemTags.ARMOR_ENCHANTABLE
        RuleRegistry.register<DamageRealizationRule>(this)
    }

    override fun onRealization(context: DamageRealizationContext) {
        if (context.rawDamage <= 0f) {
            return
        }

        val entity = context.target
        val chestplate = entity.getEquippedStack(EquipmentSlot.CHEST)
        if (!chestplate.isRedstoneSuit() || chestplate.redstoneSuitPower <= 0) {
            return
        }

        val currentDamage = context.rawDamage * context.retention
        if (currentDamage <= 0f) {
            return
        }

        val damageToReduce = (currentDamage * 0.4).coerceAtMost(chestplate.redstoneSuitPower * 4.0).toFloat()
        val powerUsage = damageToReduce / 4
        chestplate.redstoneSuitPower -= powerUsage.toLong()
        val newDamage = (currentDamage - damageToReduce).coerceAtLeast(0f)
        context.retention = newDamage / context.rawDamage
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneChestplateDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}
