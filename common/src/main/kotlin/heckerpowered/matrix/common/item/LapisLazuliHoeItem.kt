/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.HoeItem

object LapisLazuliHoeItem : HoeItem(
    ModToolMaterials.lapisLazuli,
    -2.0F, -1.0F,
    Properties().setId(ModItemIds.lapisLazuliHoe)
)