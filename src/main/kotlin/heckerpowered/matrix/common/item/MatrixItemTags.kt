/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

object MatrixItemTags {
    private fun of(id: String): TagKey<Item> {
        return TagKey.of(RegistryKeys.ITEM, Matrix.identifier(id))
    }

    val wizardHelmetTag = of("wizard_helmet")
}