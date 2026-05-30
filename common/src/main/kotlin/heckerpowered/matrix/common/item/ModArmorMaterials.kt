/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.item

import heckerpowered.matrix.common.item.ModItemTags.repairsWardenArmor
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.equipment.ArmorMaterial
import net.minecraft.world.item.equipment.ArmorType

object ModArmorMaterials {
    val warden = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 4,
            ArmorType.CHESTPLATE to 9,
            ArmorType.LEGGINGS to 7,
            ArmorType.BOOTS to 4,
            ArmorType.BODY to 12
        ),
        15, SoundEvents.ARMOR_EQUIP_NETHERITE,3.0F, 0.1F,
        repairsWardenArmor,
        MatrixEquipmentAssets.warden
    )

    val redstone = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 2,
            ArmorType.CHESTPLATE to 7,
            ArmorType.LEGGINGS to 6,
            ArmorType.BOOTS to 2,
            ArmorType.BODY to 6
        ),
        13, SoundEvents.ARMOR_EQUIP_IRON,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.redstone
    )

    val lapisLazuli = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 2,
            ArmorType.CHESTPLATE to 6,
            ArmorType.LEGGINGS to 5,
            ArmorType.BOOTS to 2,
            ArmorType.BODY to 6
        ),
        50, SoundEvents.ARMOR_EQUIP_IRON,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.lapisLazuli
    )

    val emerald = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 3,
            ArmorType.CHESTPLATE to 7,
            ArmorType.LEGGINGS to 7,
            ArmorType.BOOTS to 3,
            ArmorType.BODY to 11
        ),
        12, SoundEvents.ARMOR_EQUIP_IRON,2.5F, 0.05F,
        repairsWardenArmor,
        MatrixEquipmentAssets.emerald
    )

    val coal = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 1,
            ArmorType.CHESTPLATE to 5,
            ArmorType.LEGGINGS to 4,
            ArmorType.BOOTS to 1,
            ArmorType.BODY to 4
        ),
        12, SoundEvents.ARMOR_EQUIP_IRON,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.coal
    )

    val stone = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 1,
            ArmorType.CHESTPLATE to 4,
            ArmorType.LEGGINGS to 3,
            ArmorType.BOOTS to 1,
            ArmorType.BODY to 4
        ),
        15, SoundEvents.ARMOR_EQUIP_IRON,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.stone
    )

    val wooden = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 1,
            ArmorType.CHESTPLATE to 3,
            ArmorType.LEGGINGS to 2,
            ArmorType.BOOTS to 1,
            ArmorType.BODY to 3
        ),
        15, SoundEvents.ARMOR_EQUIP_GENERIC,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.wooden
    )

    val wizard = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 0,
            ArmorType.CHESTPLATE to 0,
            ArmorType.LEGGINGS to 0,
            ArmorType.BOOTS to 0,
            ArmorType.BODY to 0
        ),
        100, SoundEvents.ARMOR_EQUIP_GENERIC,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.wizard
    )

    val lightning = ArmorMaterial(15,
        mapOf(
            ArmorType.HELMET to 0,
            ArmorType.CHESTPLATE to 0,
            ArmorType.LEGGINGS to 0,
            ArmorType.BOOTS to 0,
            ArmorType.BODY to 0
        ),
        10, SoundEvents.ARMOR_EQUIP_GENERIC,0F, 0F,
        repairsWardenArmor,
        MatrixEquipmentAssets.lightning
    )

    fun onInitialize() {
    }
}