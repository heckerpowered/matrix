/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object WoodenLeggingsItem : Item(
    Properties().setId(ModItemIds.woodenLeggings)
        .humanoidArmor(ModArmorMaterials.wooden, ArmorType.LEGGINGS)
)