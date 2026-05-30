/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitMaxPower
import heckerpowered.matrix.common.item.ModComponents.redstoneSuitPower
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.utility.getOtherEntities
import heckerpowered.matrix.core.utility.withinDistance
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer

object RedstoneLeggingsItem : Item(
    Properties().humanoidArmor(ModArmorMaterials.redstone, ArmorType.LEGGINGS)
        .component(redstoneSuitMaxPower, 20)
        .component(redstoneSuitPower, 0)
), RedstoneSuit, TooltipProvider, DamageOutcomeRule {

    init {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val entity = context.target
        val leggings = entity.getItemBySlot(EquipmentSlot.LEGS)
        if (!leggings.isRedstoneSuit() || leggings.redstoneSuitPower <= 0) return

        --leggings.redstoneSuitPower
        if ((0 until 100).random() < 33) {
            powerLeakage(entity)
        }
    }

    private fun powerLeakage(entity: LivingEntity) {
        val level = entity.level() as? ServerLevel ?: return
        entity.getOtherEntities(6.0)
            .withinDistance(entity, 6.0)
            .forEach {
                it.hurtServer(level, entity.damageSources().mobAttack(entity), 1.0f)
            }
    }

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        RedstoneSuit.appendTooltip(components, consumer)
        consumer.accept(
            MatrixLanguage.redstoneLeggingsDescription.copy()
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
    }
}
