/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.combat.damage.DamageComputationContext
import heckerpowered.matrix.common.combat.damage.DamageComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Rarity

object MagicTalismanItem : Item(
    Settings()
        .fireproof()
        .maxCount(1)
        .rarity(Rarity.EPIC)
), DamageComputationRule {
    init {
        RuleRegistry.register<DamageComputationRule>(this)
    }

    override fun onComputation(context: DamageComputationContext) {
        val target = context.target
        if (target !is ServerPlayerEntity) {
            return
        }

        if (target.inventory.contains(ItemStack(MagicTalismanItem))) {
            target.inventory.removeOne(ItemStack(MagicTalismanItem))
            context.damageMultiplier -= 0.9
        }
    }
}
