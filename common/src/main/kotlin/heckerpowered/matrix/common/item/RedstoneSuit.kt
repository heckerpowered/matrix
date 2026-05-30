/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.function.Consumer

interface RedstoneSuit {
    companion object {
        fun appendTooltip(components: DataComponentGetter, consumer: Consumer<Component>) {
            val redstoneSuitPower = components.getOrDefault(ModComponents.redstoneSuitPower, 0.0)
            val redstoneSuitMaxPower = components.getOrDefault(ModComponents.redstoneSuitMaxPower, 0.0)
            val component = MatrixLanguage.redstoneSuitPower.copy()
                .append("$redstoneSuitPower/$redstoneSuitMaxPower")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            consumer.accept(component)
        }
    }
}

fun ItemStack.isRedstoneSuit(): Boolean {
    return item is RedstoneSuit
}

var ItemStack.redstoneSuitMaxPower: Long
    get() = getOrDefault(ModComponents.redstoneSuitMaxPower, 0).coerceAtLeast(0)
    set(value) {
        set(ModComponents.redstoneSuitMaxPower, value.coerceAtLeast(0))
    }

var ItemStack.redstoneSuitPower: Long
    get() = getOrDefault(ModComponents.redstoneSuitPower, 0).coerceAtLeast(0)
    set(value) {
        set(ModComponents.redstoneSuitPower, value.coerceIn(0..redstoneSuitMaxPower))
    }