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

object MatrixArmorMaterials {
    private fun registerMaterial(
        identifier: String,
        defensePoints: Map<ArmorItem.Type, Int>,
        enchantability: Int,
        equipSound: RegistryEntry<SoundEvent>,
        thoughness: Float,
        knockbackResistance: Float,
        dyeable: Boolean,
        repairIngredientSupplier: () -> Ingredient
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
            ArmorItem.Type.HELMET to 3,
            ArmorItem.Type.CHESTPLATE to 8,
            ArmorItem.Type.LEGGINGS to 6,
            ArmorItem.Type.BOOTS to 3,
            ArmorItem.Type.BODY to 11
        ),
        15, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, false
    ) {
        Ingredient.ofItems(*arrayOf(Items.NETHERITE_INGOT))
    }

    fun onInitialize() {

    }
}