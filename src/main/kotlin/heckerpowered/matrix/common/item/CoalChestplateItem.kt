/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object CoalChestplateItem : ArmorItem(
    coalArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(15))
)