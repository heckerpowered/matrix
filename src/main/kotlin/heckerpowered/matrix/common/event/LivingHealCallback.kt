/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ActionResult

data class LivingHealEvent(val entity: LivingEntity, var amount: Float)

fun interface LivingHealCallback {
    companion object {
        @JvmField
        val EVENT: Event<LivingHealCallback> =
            EventFactory.createArrayBacked(LivingHealCallback::class.java) { listeners ->
                LivingHealCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onHeal(event)
                        if (result != ActionResult.PASS) {
                            return@LivingHealCallback result
                        }
                    }

                    return@LivingHealCallback ActionResult.PASS
                }
            }
    }

    fun onHeal(event: LivingHealEvent): ActionResult
}