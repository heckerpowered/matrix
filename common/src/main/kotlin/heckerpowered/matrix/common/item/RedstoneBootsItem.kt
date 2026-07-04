/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitPower
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer

object RedstoneBootsItem : Item(
    Properties().setId(ModItemIds.redstoneBoots).humanoidArmor(ModArmorMaterials.redstone, ArmorType.BOOTS)
        .component(redstoneSuitMaxPower, 20)
        .component(redstoneSuitPower, 0)
), RedstoneSuit, TooltipProvider {
    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        RedstoneSuit.appendTooltip(components, consumer)
        consumer.accept(
            MatrixLanguage.redstoneBootsDescription.copy()
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
    }
}