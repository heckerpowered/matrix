/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.equipment.ArmorType

object LapisLazuliChestplateItem : Item(
    Properties().humanoidArmor(ModArmorMaterials.lapisLazuli, ArmorType.CHESTPLATE)
)