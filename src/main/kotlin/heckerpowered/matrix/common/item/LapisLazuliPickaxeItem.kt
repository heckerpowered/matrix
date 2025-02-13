package heckerpowered.matrix.common.item

import net.minecraft.item.PickaxeItem

object LapisLazuliPickaxeItem : PickaxeItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 1.0F, -2.8F))
)