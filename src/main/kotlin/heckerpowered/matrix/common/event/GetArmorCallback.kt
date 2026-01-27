/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.event

import heckerpowered.matrix.core.Accumulator
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity

fun interface GetArmorCallback {
    companion object {
        @JvmField
        val EVENT: Event<GetArmorCallback> =
            EventFactory.createArrayBacked(GetArmorCallback::class.java) { listeners ->
                GetArmorCallback { entity, accumulator ->
                    for (listener in listeners) {
                        listener.getArmor(entity, accumulator)
                    }
                }
            }
    }

    fun getArmor(entity: LivingEntity, accumulator: Accumulator)
}