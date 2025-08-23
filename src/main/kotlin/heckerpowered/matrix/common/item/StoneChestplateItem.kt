/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object StoneChestplateItem : ArmorItem(
    stoneArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(10))
)