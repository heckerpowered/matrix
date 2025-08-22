/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Rarity

object MagicTalismanItem : Item(
    Settings()
        .fireproof()
        .maxCount(1)
        .rarity(Rarity.EPIC)
) {
    init {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun onLivingHurt(accumulator: DamageAccumulator): ActionResult {
        val target = accumulator.target
        if (target !is ServerPlayerEntity) {
            return ActionResult.PASS
        }

        if (target.inventory.contains(ItemStack(MagicTalismanItem))) {
            target.inventory.removeOne(ItemStack(MagicTalismanItem))
            accumulator.damageReductionMultiplier -= 0.9
        }

        return ActionResult.PASS
    }
}