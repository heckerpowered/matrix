package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object CoalLeggingsItem : ArmorItem(
    coalArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(15))
)