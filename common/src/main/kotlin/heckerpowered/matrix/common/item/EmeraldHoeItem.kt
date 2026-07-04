/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.HoeItem

object EmeraldHoeItem : HoeItem(
    ModToolMaterials.emerald,
    -3.5F, 0.0F,
    Properties().setId(ModItemIds.emeraldHoe)
)