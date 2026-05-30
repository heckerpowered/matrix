/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object ModCreativeTab {
    val creativeTabKey = createKey("creative_tab")
    val creativeTab = FabricCreativeModeTab.builder()
        .icon { ItemStack(WardenChestplateItem) }
        .title(Component.translatable("creativeTab.matrix"))
        .displayItems { parameters, output ->
            for (item in ModItems) {
                output.accept(item)
            }
        }
        .build()

    private fun createKey(@Suppress("SameParameterValue") id: String): ResourceKey<CreativeModeTab> {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Matrix.identifier(id))
    }

    fun onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, creativeTabKey, creativeTab)
    }
}