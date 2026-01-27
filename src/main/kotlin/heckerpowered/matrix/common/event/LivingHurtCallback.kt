/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.util.ActionResult

fun interface LivingHurtCallback {
    companion object {
        @JvmField
        val EVENT: Event<LivingHurtCallback> =
            EventFactory.createArrayBacked(LivingHurtCallback::class.java) { listeners ->
                LivingHurtCallback { event ->
                    for (listener in listeners) {
                        val result = listener.onHurt(event)
                        if (result != ActionResult.PASS) {
                            return@LivingHurtCallback result
                        }
                    }

                    return@LivingHurtCallback ActionResult.PASS
                }
            }
    }

    fun onHurt(event: DamageAccumulator): ActionResult
}