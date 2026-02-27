/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object RedstoneSwordItem : SwordItem(
    redstoneToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(redstoneToolMaterial, 3, -2.4F))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit, DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        val redstoneSword =
            attacker.handItems.find { it.item is RedstoneSwordItem } ?: return
        if (redstoneSword.redstoneSuitPower <= 0) {
            return
        }

        context.baseDamageBonus += 2
        --redstoneSword.redstoneSuitPower
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneSwordDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}
