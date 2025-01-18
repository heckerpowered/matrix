package heckerpowered.matrix

import heckerpowered.matrix.data.enchantment.EnchantmentGenerator
import heckerpowered.matrix.data.language.MatrixModChineseLangProvider
import heckerpowered.matrix.data.language.MatrixModEnglishLangProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
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
    }
}