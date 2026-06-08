/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity

object MagicTalismanItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.magicTalisman)
        .fireResistant()
        .stacksTo(1)
        .rarity(Rarity.EPIC)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        if (target !is ServerPlayer) return

        val item = target.inventory.find { it.item is MagicTalismanItem } ?: return
        item.shrink(1)
        context.damageMultiplier -= 0.9
    }
}
