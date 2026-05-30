/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.tags.BlockTags
import net.minecraft.world.item.ToolMaterial

object ModToolMaterials {
    val redstone = ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 906, 7.0F,2.0F,12, ModItemTags.repairsRedstoneArmor)
    val lapisLazuli = ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 578, 6.0F, 2.0F, 50, ModItemTags.repairsLapisLazuliArmor)
    val emerald = ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1796, 8.5F, 3.5F, 12, ModItemTags.repairsEmeraldArmor)
    val coal = ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 191, 5F, 1.5F, 10, ModItemTags.repairsCoalArmor)
}