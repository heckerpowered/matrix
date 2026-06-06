package heckerpowered.matrix.common.item

import heckerpowered.matrix.Matrix
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.equipment.EquipmentAsset
import net.minecraft.world.item.equipment.EquipmentAssets

object MatrixEquipmentAssets {
    val warden = createId("warden")
    val redstone = createId("redstone")
    val lapisLazuli = createId("lapis_lazuli")
    val emerald = createId("emerald")
    val coal = createId("coal")
    val stone = createId("stone")
    val wooden = createId("wooden")
    val wizard = createId("wizard")
    val lightning = createId("lightning")

    fun createId(name: String): ResourceKey<EquipmentAsset> {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Matrix.identifier(name))
    }
}