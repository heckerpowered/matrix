/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object EmeraldLeggingsItem : ArmorItem(
    emeraldArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(35))
)