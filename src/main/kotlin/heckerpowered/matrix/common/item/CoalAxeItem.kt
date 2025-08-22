/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.AxeItem

object CoalAxeItem : AxeItem(
    coalToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(coalToolMaterial, 6.5F, -3.15F))
)