/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

object RedstoneHelmetItem : ArmorItem(
    redstoneArmorMaterial,
    Type.HELMET,
    Settings()
        .maxDamage(Type.HELMET.getMaxDamage(24))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit {
    init {
        EntityTickCallback.EVENT.register(this::onEntityTick)
    }

    private fun onEntityTick(entity: LivingEntity) {
        val helmet = entity.getEquippedStack(EquipmentSlot.HEAD)
        if (!helmet.isRedstoneSuit()) {
            return
        }

        if (entity.isSubmergedInWater && (entity.age % 100 == 0)) {
            --helmet.redstoneSuitPower
        }
        if (isUnderDaylight(entity) && (entity.age % 200 == 0)) {
            entity.equippedItems
                .filter { it.isRedstoneSuit() }
                .forEach { ++it.redstoneSuitPower }
        }
    }

    private fun isUnderDaylight(entity: LivingEntity): Boolean {
        val eyeBlockPos = BlockPos.ofFloored(entity.x, entity.eyeY, entity.z)
        return entity.world.isDay && entity.world.isSkyVisible(eyeBlockPos)
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneHelmetDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}