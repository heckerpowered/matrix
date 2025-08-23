/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity

fun interface EntityTickCallback {
    companion object {
        @JvmField
        val EVENT: Event<EntityTickCallback> =
            EventFactory.createArrayBacked(EntityTickCallback::class.java) { listeners ->
                EntityTickCallback { entity ->
                    for (listener in listeners) {
                        listener.onEntityTick(entity)
                    }
                }
            }
    }

    fun onEntityTick(entity: LivingEntity)
}