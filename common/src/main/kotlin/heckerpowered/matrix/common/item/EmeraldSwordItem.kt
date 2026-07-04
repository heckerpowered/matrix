/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.reference.ModItemIds
import net.minecraft.world.item.Item

object EmeraldSwordItem : Item(
    Properties().setId(ModItemIds.emeraldSword).sword(ModToolMaterials.emerald,3.0F, -2.4F)
)