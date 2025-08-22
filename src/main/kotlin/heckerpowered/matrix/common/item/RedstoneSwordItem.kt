/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingAttackCallback
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting

object RedstoneSwordItem : SwordItem(
    redstoneToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(redstoneToolMaterial, 3, -2.4F))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit {
    init {
        LivingAttackCallback.EVENT.register(::onLivingAttack)
    }

    private fun onLivingAttack(damageAccumulator: DamageAccumulator): ActionResult {
        val redstoneSword =
            damageAccumulator.attacker!!.handItems.find { it.item is RedstoneSwordItem } ?: return ActionResult.PASS
        if (redstoneSword.redstoneSuitPower <= 0) {
            return ActionResult.PASS
        }

        damageAccumulator.baseDamageBonus += 2
        --redstoneSword.redstoneSuitPower

        return ActionResult.PASS
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