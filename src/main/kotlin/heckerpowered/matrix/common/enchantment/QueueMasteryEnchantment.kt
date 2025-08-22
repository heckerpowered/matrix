/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.persistent.allChannelSequences
import net.minecraft.util.ActionResult

object QueueMasteryEnchantment {
    fun onInitialize() {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        // Queue Mastery: +15% damage against enemies with a locked queue.
        val lockedCount = event.target.allChannelSequences.values.count { it.locked }
        event.damageMultiplier += 0.15 * lockedCount
        return ActionResult.PASS
    }
}