/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.equipment.ArmorType

object CoalLeggingsItem : Item(
    Properties().setId(ModItemIds.coalLeggings).humanoidArmor(ModArmorMaterials.coal, ArmorType.LEGGINGS)
)