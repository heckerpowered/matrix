/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix

import heckerpowered.matrix.common.MatrixServerPlayNetworking
import heckerpowered.matrix.common.command.MatrixCommands
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.enchantment.ModEnchantments
import heckerpowered.matrix.common.entity.ModEntityTypes
import heckerpowered.matrix.common.entity.attribute.MatrixEntityAttributes
import heckerpowered.matrix.common.item.MatrixPotions
import heckerpowered.matrix.common.item.ModComponents
import heckerpowered.matrix.common.item.ModCreativeTab
import heckerpowered.matrix.common.item.ModItems
import heckerpowered.matrix.common.magic.system.MagicSystem
import heckerpowered.matrix.MatrixCommonProxy
import heckerpowered.matrix.common.recipe.RedstoneSuitChargeRecipe
import heckerpowered.matrix.common.network.syncher.ModEntityDataSerializers
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Matrix : ModInitializer {
    const val MOD_ID = "matrix"

    @JvmField
    val LOGGER: Logger = LoggerFactory.getLogger("matrix")

    @JvmField
    var proxy: MatrixCommonProxy = MatrixCommonProxy()

    override fun onInitialize() {
        MatrixServerPlayNetworking.onInitialize()
        MagicSystem.onInitialize()
        ModMobEffects.onInitialize()
        ModEnchantments.onInitialize()
        MatrixCommands.onInitialize()
        ModComponents.onInitialize()
        ModEntityDataSerializers.onInitialize()
        ModItems.onInitialize()
        ModCreativeTab.onInitialize()
        MatrixPotions.onInitialize()
        ModEntityTypes.onInitialize()
        MatrixEntityAttributes.onInitialize()
        RedstoneSuitChargeRecipe.onInitialize()
    }

    @JvmStatic
    fun identifier(path: String): Identifier {
        return Identifier.fromNamespaceAndPath("matrix", path)
    }
}
