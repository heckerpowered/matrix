/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.AxeItem

object EmeraldAxeItem : AxeItem(
    ModToolMaterials.emerald,
    5.0F, -3.0F,
    Properties().setId(ModItemIds.emeraldAxe)
)