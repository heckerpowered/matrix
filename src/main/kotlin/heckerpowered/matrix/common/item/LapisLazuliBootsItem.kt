/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object LapisLazuliBootsItem : ArmorItem(
    lapisLazuliArmorMaterial,
    Type.BOOTS,
    Settings()
        .maxDamage(20)
)