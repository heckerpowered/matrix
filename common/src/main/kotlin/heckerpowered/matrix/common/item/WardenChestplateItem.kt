/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.entity.rule.KnockbackContext
import heckerpowered.matrix.common.entity.rule.KnockbackRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipProvider
import net.minecraft.world.item.equipment.ArmorType
import java.util.function.Consumer

// TODO: EntityPolarity
// TODO: Restrict negative effects
object WardenChestplateItem : Item(
    Properties().humanoidArmor(ModArmorMaterials.warden, ArmorType.CHESTPLATE)
        .fireResistant()
        .rarity(Rarity.EPIC)
), TooltipProvider, DamageComputationRule, KnockbackRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
        RuleRegistry.register<KnockbackRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving() ?: return
        if (isAngered(attacker)) {
            context.damageMultiplier += 1
        }
        if (isAngered(context.target)) {
            context.cancel()
        }
    }

    override fun onKnockback(context: KnockbackContext) {
        if (isAngered(context.entity)) {
            context.cancel()
        }
    }

    @JvmStatic
    fun isAngered(entity: LivingEntity): Boolean {
        try {
            if (entity.getItemBySlot(EquipmentSlot.CHEST).item != this) {
                return false
            }

            return entity.isWardenArmorAngered()
        } catch (_: NullPointerException) {
            // Is angered may be called when the entity is not initialized yet
            return false
        }
    }

    private var tooltipHash = 0
    private var tooltips = listOf<Component>()

    override fun addToTooltip(context: TooltipContext, consumer: Consumer<Component>, flag: TooltipFlag, components: DataComponentGetter) {
        val description = MatrixLanguage.wardenChestplateDescription.string
        val hash = description.hashCode()
        if (hash != tooltipHash) {
            tooltipHash = hash
            tooltips = description.lineSequence()
                .map { line ->
                    Component.literal(line).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                }
                .toList()
        }

        for (tooltip in tooltips) {
            consumer.accept(tooltip)
        }
    }
}
