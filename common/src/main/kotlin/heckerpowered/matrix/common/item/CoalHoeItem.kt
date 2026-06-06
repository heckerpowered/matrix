/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.HoeItem

object CoalHoeItem : HoeItem(
    ModToolMaterials.coal,
    -1.5F, -1.0F,
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.coalHoe)
)