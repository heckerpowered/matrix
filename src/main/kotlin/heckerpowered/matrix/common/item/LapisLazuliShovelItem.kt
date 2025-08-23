/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ShovelItem

object LapisLazuliShovelItem : ShovelItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 1.5F, -3.0F))
)