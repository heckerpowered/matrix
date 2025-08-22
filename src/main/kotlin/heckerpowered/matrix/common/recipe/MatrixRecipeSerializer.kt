/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.common.recipe

import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.SpecialRecipeSerializer
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object MatrixRecipeSerializer {
    private fun <S : RecipeSerializer<T>?, T : Recipe<*>?> register(identifier: String, serializer: S): S {
        return Registry.register(Registries.RECIPE_SERIALIZER, identifier, serializer)
    }

    val redstoneSuitChargerRecipeSerializer =
        register("redstone_suit_charger", SpecialRecipeSerializer { RedstoneSuitChargeRecipe(it) })

    fun onInitialize() {
        // NO-OP
    }
}