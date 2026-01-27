/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.magic.channel.ChannelQueue.Companion.allChannelQueues
import net.minecraft.util.ActionResult

object QueueMasteryEnchantment {
    fun onInitialize() {
        LivingHurtCallback.EVENT.register(::onLivingHurt)
    }

    private fun onLivingHurt(event: DamageAccumulator): ActionResult {
        // Queue Mastery: +15% damage against enemies with a locked queue.
        val lockedCount = event.target.allChannelQueues.values.count { it.isLocked }
        event.damageMultiplier += 0.15 * lockedCount
        return ActionResult.PASS
    }
}