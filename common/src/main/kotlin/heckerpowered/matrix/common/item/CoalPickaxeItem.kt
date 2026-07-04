/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item

object CoalPickaxeItem : Item(
    Properties().setId(ModItemIds.coalPickaxe).pickaxe(ModToolMaterials.coal, 1.0F, -2.8F)
)