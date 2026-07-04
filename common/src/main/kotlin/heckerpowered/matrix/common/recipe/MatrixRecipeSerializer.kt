/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.recipe

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier

/**
 * Restored from the last working 1.21 build (matrix-1.0.0.jar): the recipe serializer
 * registration was lost from the source tree. The original registered with the plain
 * string id, i.e. under the `minecraft` namespace ("minecraft:redstone_suit_charger"),
 * which the generated recipe data references — kept identical for data compatibility.
 */
object MatrixRecipeSerializer {
    fun onInitialize() {
        Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            Identifier.withDefaultNamespace("redstone_suit_charger"),
            RedstoneSuitChargeRecipe.serializer
        )
    }
}
