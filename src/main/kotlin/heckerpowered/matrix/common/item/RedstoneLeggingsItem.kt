/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_MAX_POWER
import heckerpowered.matrix.common.item.MatrixComponents.REDSTONE_SUIT_POWER
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object RedstoneLeggingsItem : ArmorItem(
    redstoneArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(24))
        .component(REDSTONE_SUIT_MAX_POWER, 20)
        .component(REDSTONE_SUIT_POWER, 0)
), RedstoneSuit, DamageOutcomeRule {

    init {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val entity = context.target
        val leggings = entity.getEquippedStack(EquipmentSlot.LEGS)
        if (!leggings.isRedstoneSuit() || leggings.redstoneSuitPower <= 0) {
            return
        }

        --leggings.redstoneSuitPower
        if ((0..100).random() in 0..33) {
            powerLeakage(entity)
        }
    }

    private fun powerLeakage(entity: LivingEntity) {
        entity.world.getOtherEntities(entity, entity.boundingBox.expand(6.0)).forEach {
            it.damage(entity.damageSources.mobAttack(entity), 1.0f)
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Text>,
        type: TooltipType,
    ) {
        super.appendTooltip(stack, context, tooltip, type)
        RedstoneSuit.appendTooltip(stack, context, tooltip, type)
        tooltip.add(MatrixLanguage.redstoneLeggingsDescription.copy().formatted(Formatting.GRAY, Formatting.ITALIC))
    }
}
