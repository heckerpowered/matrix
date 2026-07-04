/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object WoodenBootsItem : Item(
    Properties().setId(ModItemIds.woodenBoots)
        .humanoidArmor(ModArmorMaterials.wooden, ArmorType.BOOTS)
)