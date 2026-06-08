/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.data.tag

import heckerpowered.matrix.common.tag.MatrixDamageTypeTags
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagAppender
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class ModDamageTypeProvider(
    fabricPackOutput: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricTagsProvider<DamageType>(fabricPackOutput, Registries.DAMAGE_TYPE, registriesFuture) {
    private fun getOrCreateTagBuilder(tag: TagKey<DamageType>): TagAppender<DamageType> {
        val rawBuilder = getOrCreateRawBuilder(tag)
        return TagAppender.forBuilder(rawBuilder)
    }

    override fun addTags(registries: HolderLookup.Provider) {
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_SHIELD)
            .addOptional(MatrixDamageTypes.magic)

        getOrCreateTagBuilder(MatrixDamageTypeTags.magic)
            .addOptional(MatrixDamageTypes.magic)
    }
}
