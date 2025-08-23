/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.HoeItem

object LapisLazuliHoeItem : HoeItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, -2.0F, -1.0F))
)