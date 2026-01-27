/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix

import heckerpowered.matrix.common.MatrixCommonProxy
import heckerpowered.matrix.common.MatrixServerPlayNetworking
import heckerpowered.matrix.common.command.MatrixCommands
import heckerpowered.matrix.common.effect.MatrixStatusEffects
import heckerpowered.matrix.common.enchantment.MatrixEnchantments
import heckerpowered.matrix.common.entity.MatrixEntityType
import heckerpowered.matrix.common.entity.attribute.MatrixEntityAttributes
import heckerpowered.matrix.common.item.MatrixComponents
import heckerpowered.matrix.common.item.MatrixItemGroups
import heckerpowered.matrix.common.item.MatrixItems
import heckerpowered.matrix.common.item.MatrixPotions
import heckerpowered.matrix.common.magic.MagicManager
import heckerpowered.matrix.common.recipe.MatrixRecipeSerializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Matrix : ModInitializer {
    const val MOD_ID = "matrix"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("matrix")

    var proxy = MatrixCommonProxy()

    override fun onInitialize() {
        MatrixServerPlayNetworking.onInitialize()
        MagicManager.onInitialize()
        MatrixStatusEffects.onInitialize()
        MatrixEnchantments.onInitialize()
        MatrixCommands.onInitialize()
        MatrixComponents.onInitialize()
        MatrixItems.onInitialize()
        MatrixItemGroups.onInitialize()
        MatrixRecipeSerializer.onInitialize()
        MatrixPotions.onInitialize()
        MatrixEntityType.onInitialize()
        MatrixEntityAttributes.onInitialize()
    }

    @JvmStatic
    fun identifier(path: String): Identifier {
        return Identifier.of("matrix", path)
    }
}