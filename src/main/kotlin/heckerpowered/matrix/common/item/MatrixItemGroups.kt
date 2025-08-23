/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text

val itemGroupKey = MatrixItemGroups.itemGroupKey
val itemGroup = MatrixItemGroups.itemGroup

object MatrixItemGroups {
    val itemGroupKey: RegistryKey<ItemGroup> =
        RegistryKey.of(Registries.ITEM_GROUP.key, Matrix.identifier("item_group"))
    val itemGroup: ItemGroup = FabricItemGroup.builder()
        .icon { ItemStack(WardenChestplateItem) }
        .displayName(Text.translatable("itemGroup.matrix"))
        .build()

    fun onInitialize() {
        Registry.register(Registries.ITEM_GROUP, itemGroupKey, itemGroup)

        ItemGroupEvents.modifyEntriesEvent(itemGroupKey).register { itemGroup ->
            allMatrixItems.forEach {
                itemGroup.add(it)
            }
        }
    }
}