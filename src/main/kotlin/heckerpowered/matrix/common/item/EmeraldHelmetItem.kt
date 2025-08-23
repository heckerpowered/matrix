/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object EmeraldHelmetItem : ArmorItem(
    emeraldArmorMaterial,
    Type.HELMET,
    Settings()
        .maxDamage(Type.HELMET.getMaxDamage(35))
)