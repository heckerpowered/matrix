/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.HoeItem

object CoalHoeItem : HoeItem(
    coalToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(coalToolMaterial, -1.5F, -1.0F))
)