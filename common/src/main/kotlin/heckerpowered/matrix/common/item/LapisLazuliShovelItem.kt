/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.ShovelItem

object LapisLazuliShovelItem : ShovelItem(
    ModToolMaterials.lapisLazuli,
    1.5F, -3.0F,
    Properties().setId(heckerpowered.matrix.common.reference.ModItemIds.lapisLazuliShovel)
)