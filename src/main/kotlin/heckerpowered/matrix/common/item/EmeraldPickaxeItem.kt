package heckerpowered.matrix.common.item

import net.minecraft.item.PickaxeItem

object EmeraldPickaxeItem : PickaxeItem(
    emeraldToolMaterial,
    Settings()
        .attributeModifiers(createAttributeModifiers(emeraldToolMaterial, 1.0F, -2.8F))
)