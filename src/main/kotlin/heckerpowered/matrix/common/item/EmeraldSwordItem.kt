/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.SwordItem

object EmeraldSwordItem : SwordItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, 3, -2.4F))
)