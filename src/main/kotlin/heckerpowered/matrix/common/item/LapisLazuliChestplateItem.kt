/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object LapisLazuliChestplateItem : ArmorItem(
    lapisLazuliArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(20)
)