/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.item

import net.minecraft.item.ShovelItem

object EmeraldShovelItem : ShovelItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, 1.5F, -3.0F))
)