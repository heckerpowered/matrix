/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.event.LivingKnockbackCallback
import heckerpowered.matrix.common.event.LivingKnockbackEvent
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ArmorItem
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity

object WardenChestplateItem : ArmorItem(
    wardenArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .fireproof()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(37))
        .rarity(Rarity.EPIC)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
        LivingKnockbackCallback.EVENT.register(::onLivingKnockback)
    }

    override fun onComputation(context: DamageComputationContext) {
        val attacker = context.attackerAsLiving()
        if (attacker != null && isAngered(attacker)) {
            context.damageMultiplier += 1
        }

        if (isAngered(context.target)) {
            context.cancel()
        }
    }

    private fun onLivingKnockback(event: LivingKnockbackEvent): ActionResult {
        if (isAngered(event.entity)) {
            return ActionResult.FAIL
        }
        return ActionResult.PASS
    }

    override fun appendTooltip(stack: ItemStack, context: TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
        super.appendTooltip(stack, context, tooltip, type)
        val lines = MatrixLanguage.wardenChestplateDescription.string.split('\n').map {
            Text.literal(it).formatted(Formatting.GRAY, Formatting.ITALIC)
        }
        tooltip.addAll(lines)
    }

    @JvmStatic
    fun isAngered(entity: LivingEntity): Boolean {
        try {
            if (entity.getEquippedStack(EquipmentSlot.CHEST).item != this) {
                return false
            }

            return isWardenArmorAngered(entity)
        } catch (_: NullPointerException) {
            // Is angered may be called when the entity is not initialized yet
            return false
        }
    }
}
