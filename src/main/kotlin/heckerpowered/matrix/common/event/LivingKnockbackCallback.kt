/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ActionResult

data class LivingKnockbackEvent(val entity: LivingEntity, var strength: Double, var x: Double, var z: Double)

fun interface LivingKnockbackCallback {
    companion object {
        @JvmField
        val EVENT: Event<LivingKnockbackCallback> =
            EventFactory.createArrayBacked(LivingKnockbackCallback::class.java) { listeners ->
                LivingKnockbackCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onKnockback(event)
                        if (result != ActionResult.PASS) {
                            return@LivingKnockbackCallback result
                        }
                    }

                    return@LivingKnockbackCallback ActionResult.PASS
                }
            }
    }

    fun onKnockback(event: LivingKnockbackEvent): ActionResult
}