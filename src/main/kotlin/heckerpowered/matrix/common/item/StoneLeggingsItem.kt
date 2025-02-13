package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object StoneLeggingsItem : ArmorItem(
    stoneArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(10))
)