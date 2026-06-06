/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item

object CoalPickaxeItem : Item(
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.coalPickaxe).pickaxe(ModToolMaterials.coal, 1.0F, -2.8F)
)