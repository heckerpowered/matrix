/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.tag

import heckerpowered.matrix.common.item.ModItemTags
import heckerpowered.matrix.common.item.WizardHelmet
import heckerpowered.matrix.common.reference.ModItemIds
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import java.util.concurrent.CompletableFuture

class ModItemTagProvider(
    fabricPackOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricTagsProvider.ItemTagsProvider(fabricPackOutput, registriesFuture) {

    private fun addWizardHelmet() {
        val tagBuilder = builder(ModItemTags.wizardHelmetTag)
        ModItemIds
            .filterIsInstance<WizardHelmet>()
            .forEach {
                val resourceKey = BuiltInRegistries.ITEM.getResourceKey(it).orElseThrow()
                tagBuilder.add(resourceKey)
            }
    }

    override fun addTags(registries: HolderLookup.Provider) {
        addWizardHelmet()
    }
}