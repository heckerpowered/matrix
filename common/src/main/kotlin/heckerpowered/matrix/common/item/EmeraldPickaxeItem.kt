/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.world.item.Item

object EmeraldPickaxeItem : Item(
    Properties().pickaxe(ModToolMaterials.emerald, 1.0F, -2.8F)
)