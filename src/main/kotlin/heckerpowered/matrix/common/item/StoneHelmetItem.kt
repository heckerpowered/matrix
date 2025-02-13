package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object StoneHelmetItem : ArmorItem(
    stoneArmorMaterial,
    Type.HELMET,
    Settings()
        .maxDamage(Type.HELMET.getMaxDamage(10))
)