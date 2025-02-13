package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

val wardenArmorMaterial
    get() = MatrixArmorMaterials.wardenArmorMaterial

val redstoneArmorMaterial
    get() = MatrixArmorMaterials.redstoneMaterial

val lapisLazuliArmorMaterial
    get() = MatrixArmorMaterials.lapisLazuliMaterial

val emeraldArmorMaterial
    get() = MatrixArmorMaterials.emeraldMaterial

val coalArmorMaterial
    get() = MatrixArmorMaterials.coalMaterial

val stoneArmorMaterial
    get() = MatrixArmorMaterials.stoneMaterial

val woodenArmorMaterial
    get() = MatrixArmorMaterials.woodenMaterial

object MatrixArmorMaterials {
    private fun registerMaterial(
        identifier: String,
        defensePoints: Map<ArmorItem.Type, Int>,
        enchantability: Int,
        equipSound: RegistryEntry<SoundEvent>,
        thoughness: Float,
        knockbackResistance: Float,
        dyeable: Boolean,
        repairIngredientSupplier: () -> Ingredient,
    ): RegistryEntry<ArmorMaterial> {
        val layers = listOf(
            ArmorMaterial.Layer(Matrix.identifier(identifier), "", dyeable)
        )

        val armorMaterial = ArmorMaterial(
            defensePoints,
            enchantability,
            equipSound,
            repairIngredientSupplier,
            layers,
            thoughness,
            knockbackResistance
        ).let {
            Registry.register(Registries.ARMOR_MATERIAL, Matrix.identifier(identifier), it)
        }

        return RegistryEntry.of(armorMaterial)
    }

    val wardenArmorMaterial = registerMaterial(
        "warden",
        mapOf(
            ArmorItem.Type.HELMET to 4,
            ArmorItem.Type.CHESTPLATE to 9,
            ArmorItem.Type.LEGGINGS to 7,
            ArmorItem.Type.BOOTS to 4,
            ArmorItem.Type.BODY to 12
        ),
        15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, false
    ) {
        Ingredient.ofItems(*arrayOf())
    }

    val redstoneMaterial = registerMaterial(
        "redstone",
        mapOf(
            ArmorItem.Type.HELMET to 2,
            ArmorItem.Type.CHESTPLATE to 7,
            ArmorItem.Type.LEGGINGS to 6,
            ArmorItem.Type.BOOTS to 2,
            ArmorItem.Type.BODY to 6
        ),
        13, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F, 0F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.REDSTONE_BLOCK))
    }

    val lapisLazuliMaterial = registerMaterial(
        "lapis_lazuli",
        mapOf(
            ArmorItem.Type.HELMET to 2,
            ArmorItem.Type.CHESTPLATE to 6,
            ArmorItem.Type.LEGGINGS to 5,
            ArmorItem.Type.BOOTS to 2,
            ArmorItem.Type.BODY to 6
        ),
        50, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F, 0F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.LAPIS_BLOCK))
    }

    val emeraldMaterial = registerMaterial(
        "emerald",
        mapOf(
            ArmorItem.Type.HELMET to 3,
            ArmorItem.Type.CHESTPLATE to 7,
            ArmorItem.Type.LEGGINGS to 7,
            ArmorItem.Type.BOOTS to 3,
            ArmorItem.Type.BODY to 11
        ),
        12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 2.5F, 0.05F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.EMERALD))
    }

    val coalMaterial = registerMaterial(
        "coal",
        mapOf(
            ArmorItem.Type.HELMET to 1,
            ArmorItem.Type.CHESTPLATE to 5,
            ArmorItem.Type.LEGGINGS to 4,
            ArmorItem.Type.BOOTS to 1,
            ArmorItem.Type.BODY to 4
        ),
        12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F, 0F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.COAL_BLOCK))
    }

    val stoneMaterial = registerMaterial(
        "stone",
        mapOf(
            ArmorItem.Type.HELMET to 1,
            ArmorItem.Type.CHESTPLATE to 4,
            ArmorItem.Type.LEGGINGS to 3,
            ArmorItem.Type.BOOTS to 1,
            ArmorItem.Type.BODY to 4
        ),
        15, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F, 0F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.COBBLESTONE))
    }

    val woodenMaterial = registerMaterial(
        "wooden",
        mapOf(
            ArmorItem.Type.HELMET to 1,
            ArmorItem.Type.CHESTPLATE to 3,
            ArmorItem.Type.LEGGINGS to 2,
            ArmorItem.Type.BOOTS to 1,
            ArmorItem.Type.BODY to 3
        ),
        15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F, 0F, false
    ) {
        Ingredient.ofItems(
            Items.OAK_PLANKS,
            Items.DARK_OAK_PLANKS,
            Items.BIRCH_PLANKS,
            Items.ACACIA_PLANKS,
            Items.BAMBOO_PLANKS,
            Items.CHERRY_PLANKS,
            Items.CRIMSON_PLANKS,
            Items.JUNGLE_PLANKS,
            Items.MANGROVE_PLANKS,
            Items.SPRUCE_PLANKS,
            Items.WARPED_PLANKS
        )
    }

    fun onInitialize() {
    }
}