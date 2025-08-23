/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object EmeraldBootsItem : ArmorItem(
    emeraldArmorMaterial,
    Type.BOOTS,
    Settings()
        .maxDamage(Type.BOOTS.getMaxDamage(35))
)