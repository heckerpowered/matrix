/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.HoeItem

object CoalHoeItem : HoeItem(
    ModToolMaterials.coal,
    -1.5F, -1.0F,
    Properties().setId(ModItemIds.coalHoe)
)