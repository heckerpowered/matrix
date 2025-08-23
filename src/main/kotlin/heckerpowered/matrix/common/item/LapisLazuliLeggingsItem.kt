/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object LapisLazuliLeggingsItem : ArmorItem(
    lapisLazuliArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(20)
)