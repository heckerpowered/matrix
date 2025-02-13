package heckerpowered.matrix.common.item

import net.minecraft.item.ShovelItem

object EmeraldShovelItem : ShovelItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, 1.5F, -3.0F))
)