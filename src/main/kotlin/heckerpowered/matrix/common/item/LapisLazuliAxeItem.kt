package heckerpowered.matrix.common.item

import net.minecraft.item.AxeItem

object LapisLazuliAxeItem : AxeItem(
    lapisLazuliToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(lapisLazuliToolMaterial, 6.0F, -3.0F))
)