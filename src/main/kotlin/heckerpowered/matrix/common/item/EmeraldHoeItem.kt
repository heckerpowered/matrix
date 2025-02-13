package heckerpowered.matrix.common.item

import net.minecraft.item.HoeItem

object EmeraldHoeItem : HoeItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, -3.5F, 0.0F))
)