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
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.ActionResult

fun interface CanHaveStatusEffectCallback {
    companion object {
        @JvmField
        val EVENT: Event<CanHaveStatusEffectCallback> =
            EventFactory.createArrayBacked(CanHaveStatusEffectCallback::class.java) { listeners ->
                CanHaveStatusEffectCallback { entity, effect ->
                    for (listener in listeners) {
                        val result = listener.canHaveStatusEffect(entity, effect)
                        if (result != ActionResult.PASS) {
                            return@CanHaveStatusEffectCallback result
                        }
                    }

                    ActionResult.PASS
                }
            }
    }

    fun canHaveStatusEffect(entity: LivingEntity, effect: StatusEffectInstance): ActionResult
}