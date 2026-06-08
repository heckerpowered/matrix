/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object CoalChestplateItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.coalChestplate).humanoidArmor(ModArmorMaterials.coal, ArmorType.CHESTPLATE)
)