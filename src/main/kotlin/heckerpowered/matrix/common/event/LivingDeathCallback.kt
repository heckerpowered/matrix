/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.ActionResult

fun interface LivingDeathCallback {
    companion object {
        @JvmField
        val EVENT: Event<LivingDeathCallback> =
            EventFactory.createArrayBacked(LivingDeathCallback::class.java) { listeners ->
                LivingDeathCallback { entity, damageSource ->
                    for (listener in listeners) {
                        val result = listener.onDeath(entity, damageSource)
                        if (result != ActionResult.PASS) {
                            return@LivingDeathCallback result
                        }
                    }

                    return@LivingDeathCallback ActionResult.PASS
                }
            }
    }

    fun onDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult
}