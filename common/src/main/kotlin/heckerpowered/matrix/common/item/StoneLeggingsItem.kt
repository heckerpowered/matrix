/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object StoneLeggingsItem : Item(
    Properties().setId(ModItemIds.stoneLeggings).humanoidArmor(ModArmorMaterials.stone, ArmorType.LEGGINGS)
)