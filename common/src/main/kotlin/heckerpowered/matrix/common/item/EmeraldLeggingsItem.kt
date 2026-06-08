/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object EmeraldLeggingsItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.emeraldLeggings).humanoidArmor(ModArmorMaterials.emerald, ArmorType.LEGGINGS)
)