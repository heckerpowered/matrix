/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item
import net.minecraft.world.item.equipment.ArmorType

object EmeraldBootsItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.emeraldBoots).humanoidArmor(ModArmorMaterials.emerald, ArmorType.BOOTS)
)