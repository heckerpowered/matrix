/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix

import heckerpowered.matrix.common.magic.system.Magics
import heckerpowered.matrix.data.enchantment.ModEnchantmentGenerator
import heckerpowered.matrix.data.language.ModChineseLangProvider
import heckerpowered.matrix.data.language.ModEnglishLangProvider
import heckerpowered.matrix.data.recipe.ModRecipeProvider
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.data.tag.ModDamageTypeProvider
import heckerpowered.matrix.data.tag.ModItemTagProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.world.damagesource.DamageScaling
import net.minecraft.world.damagesource.DamageType

object ModDataGenerator : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        // The enchantment generator loops over the magic registry (one enchantment per magic);
        // make sure it is populated even if the mod initializer did not run first (idempotent).
        Magics.onInitialize()

        val pack = fabricDataGenerator.createPack()

        pack.addProvider(::ModChineseLangProvider)
        pack.addProvider(::ModEnglishLangProvider)
        pack.addProvider(::ModEnchantmentGenerator)
        pack.addProvider(::ModRecipeProvider)
        pack.addProvider(::ModItemTagProvider)
        pack.addProvider(::ModDamageTypeProvider)
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        Magics.onInitialize()
        registryBuilder.add(Registries.ENCHANTMENT, ModEnchantmentGenerator::bootstrap)
        // 26.2: tag providers resolve references against the built registries only, so the
        // damage type must be bootstrapped here for ModDamageTypeProvider's bypasses_shield
        // entry; values mirror data/matrix/damage_type/magic.json exactly.
        registryBuilder.add(Registries.DAMAGE_TYPE) { context ->
            context.register(MatrixDamageTypes.magic, DamageType("magic", DamageScaling.NEVER, 0.1F))
        }
    }
}