/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.entity.rule.EntityUpdateContext
import heckerpowered.matrix.common.entity.rule.EntityUpdateRule
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitPower
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer

object RedstoneHelmetItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.redstoneHelmet).humanoidArmor(ModArmorMaterials.redstone, ArmorType.HELMET)
        .component(redstoneSuitMaxPower, 20)
        .component(redstoneSuitPower, 0)
), EntityUpdateRule, RedstoneSuit, TooltipProvider {
    init {
        RuleRegistry.register<EntityUpdateRule>(this)
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity as? LivingEntity ?: return
        val helmet = entity.getItemBySlot(EquipmentSlot.HEAD)
        if (!helmet.isRedstoneSuit()) {
            return
        }

        if (entity.isInWater && (entity.tickCount % 100 == 0)) {
            --helmet.redstoneSuitPower
        }
        if (isUnderDaylight(entity) && (entity.tickCount % 200 == 0)) {
            EquipmentSlot.entries
                .map { entity.getItemBySlot(it) }
                .filter { it.isRedstoneSuit() }
                .forEach { ++it.redstoneSuitPower }
        }
    }

    private fun isUnderDaylight(entity: LivingEntity): Boolean {
        val roundedEyePos = BlockPos.containing(entity.x, entity.eyeY, entity.z)
        return entity.level().isBrightOutside && entity.level().canSeeSky(roundedEyePos)
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        RedstoneSuit.appendTooltip(components, consumer)
        consumer.accept(
            MatrixLanguage.redstoneHelmetDescription.copy()
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
    }
}