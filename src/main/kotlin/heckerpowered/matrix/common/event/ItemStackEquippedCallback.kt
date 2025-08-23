/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

fun interface ItemStackEquippedCallback {
    companion object {
        @JvmField
        val EVENT: Event<ItemStackEquippedCallback> =
            EventFactory.createArrayBacked(ItemStackEquippedCallback::class.java) { listeners ->
                ItemStackEquippedCallback { entity, equipmentSlot, previousItemStack, currentItemStack ->
                    for (listener in listeners) {
                        listener.onItemStackEquipped(entity, equipmentSlot, previousItemStack, currentItemStack)
                    }
                }
            }
    }

    fun onItemStackEquipped(
        entity: LivingEntity,
        equipmentSlot: EquipmentSlot,
        previousItemStack: ItemStack,
        currentItemStack: ItemStack,
    )
}