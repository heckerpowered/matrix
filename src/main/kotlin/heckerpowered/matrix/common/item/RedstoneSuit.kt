/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

interface RedstoneSuit {
    companion object {
        fun appendTooltip(
            stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType,
        ) {
            if (!stack.isRedstoneSuit()) {
                return
            }

            tooltip.add(
                MatrixLanguage.redstoneSuitPower.copy()
                    .append("${stack.redstoneSuitPower}/${stack.redstoneSuitMaxPower}")
                    .formatted(Formatting.GRAY, Formatting.ITALIC)
            )
        }
    }
}

fun ItemStack.isRedstoneSuit(): Boolean {
    return item is RedstoneSuit
}

var ItemStack.redstoneSuitMaxPower: Long
    get() = getOrDefault(REDSTONE_SUIT_MAX_POWER, 0).coerceAtLeast(0)
    set(value) {
        set(REDSTONE_SUIT_MAX_POWER, value.coerceAtLeast(0))
    }

var ItemStack.redstoneSuitPower: Long
    get() = getOrDefault(REDSTONE_SUIT_POWER, 0).coerceAtLeast(0)
    set(value) {
        set(REDSTONE_SUIT_POWER, value.coerceIn(0..redstoneSuitMaxPower))
    }