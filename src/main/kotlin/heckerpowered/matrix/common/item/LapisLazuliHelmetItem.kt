package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object LapisLazuliHelmetItem : ArmorItem(
    lapisLazuliArmorMaterial,
    Type.HELMET,
    Settings()
        .maxDamage(20)
)