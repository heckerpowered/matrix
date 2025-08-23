/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.PickaxeItem

object CoalPickaxeItem : PickaxeItem(
    coalToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(coalToolMaterial, 1.0F, -2.8F))
)