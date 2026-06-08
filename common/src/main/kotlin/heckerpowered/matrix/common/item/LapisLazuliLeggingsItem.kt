/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object LapisLazuliLeggingsItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.lapisLazuliLeggings).humanoidArmor(ModArmorMaterials.lapisLazuli, ArmorType.LEGGINGS)
)