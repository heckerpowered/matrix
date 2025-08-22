/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.PickaxeItem

object LapisLazuliPickaxeItem : PickaxeItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 1.0F, -2.8F))
)