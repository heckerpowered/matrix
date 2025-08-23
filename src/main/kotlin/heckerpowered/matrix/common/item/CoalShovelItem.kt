/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ShovelItem

object CoalShovelItem : ShovelItem(
    coalToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(coalToolMaterial, 1.5F, -3.0F))
)