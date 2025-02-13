package heckerpowered.matrix.common.item

import net.minecraft.item.AxeItem

object EmeraldAxeItem : AxeItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, 5.0F, -3.0F))
)