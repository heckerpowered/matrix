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
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Consumer

object RedstoneHoeItem : HoeItem(
    ModToolMaterials.redstone, -2.0F, -1.0F,
    Properties().setId(ModItemIds.redstoneHoe)
        .component(redstoneSuitMaxPower, 20)
        .component(redstoneSuitPower, 0)
), RedstoneSuit, TooltipProvider {
    override fun getDestroySpeed(itemStack: ItemStack, state: BlockState): Float {
        val destroySpeed = super.getDestroySpeed(itemStack, state)
        if (itemStack.redstoneSuitPower > 0) {
            return destroySpeed * 1.4F
        }
        return destroySpeed
    }

    override fun mineBlock(itemStack: ItemStack, level: Level, state: BlockState, pos: BlockPos, owner: LivingEntity): Boolean {
        --itemStack.redstoneSuitPower
        return super.mineBlock(itemStack, level, state, pos, owner)
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        RedstoneSuit.appendTooltip(components, consumer)
        consumer.accept(
            MatrixLanguage.redstoneMiningToolDescription.copy()
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
    }
}