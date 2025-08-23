/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

import heckerpowered.matrix.core.Accumulator
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.entry.RegistryEntry

fun interface AccumulateAttributeValueCallback {
    companion object {
        @JvmField
        val EVENT: Event<AccumulateAttributeValueCallback> =
            EventFactory.createArrayBacked(AccumulateAttributeValueCallback::class.java) { listeners ->
                AccumulateAttributeValueCallback { entity, attribute, accumulator ->
                    for (listener in listeners) {
                        listener.getAttributeValue(entity, attribute, accumulator)
                    }
                }
            }
    }

    fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator)
}