/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix

import heckerpowered.matrix.data.enchantment.ModEnchantmentGenerator
import heckerpowered.matrix.data.language.ModChineseLangProvider
import heckerpowered.matrix.data.language.ModEnglishLangProvider
import heckerpowered.matrix.data.recipe.ModRecipeProvider
import heckerpowered.matrix.data.tag.ModDamageTypeProvider
import heckerpowered.matrix.data.tag.ModItemTagProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries

object ModDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()

        pack.addProvider(::ModChineseLangProvider)
        pack.addProvider(::ModEnglishLangProvider)
        pack.addProvider(::ModEnchantmentGenerator)
        pack.addProvider(::ModRecipeProvider)
        pack.addProvider(::ModItemTagProvider)
        pack.addProvider(::ModDamageTypeProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, ModEnchantmentGenerator::bootstrap)
    }
}