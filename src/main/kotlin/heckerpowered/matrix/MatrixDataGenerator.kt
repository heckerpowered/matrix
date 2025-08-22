/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix

import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.enchantment.EnchantmentGenerator
import heckerpowered.matrix.data.language.MatrixModChineseLangProvider
import heckerpowered.matrix.data.language.MatrixModEnglishLangProvider
import heckerpowered.matrix.data.recipe.MatrixRecipeProvider
import heckerpowered.matrix.data.tag.MatrixDamageTypeProvider
import heckerpowered.matrix.data.tag.MatrixItemTagProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.entity.damage.DamageScaling
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryBuilder
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

object MatrixDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()

        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            MatrixModChineseLangProvider(output, registriesFuture)
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            MatrixModEnglishLangProvider(output, registriesFuture)
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            EnchantmentGenerator(output, registriesFuture)
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            MatrixRecipeProvider(output, registriesFuture)
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            MatrixItemTagProvider(output, registriesFuture)
        }
        pack.addProvider { output: FabricDataOutput, registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup> ->
            MatrixDamageTypeProvider(output, registriesFuture)
        }
    }

    override fun buildRegistry(registryBuilder: RegistryBuilder) {
        super.buildRegistry(registryBuilder)
        registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE) {
            it.register(MatrixDamageTypes.magic, DamageType("magic", DamageScaling.NEVER, 0.1F))
        }
    }
}