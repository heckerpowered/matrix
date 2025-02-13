package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object CoalBootsItem : ArmorItem(
    coalArmorMaterial,
    Type.BOOTS,
    Settings()
        .maxDamage(Type.BOOTS.getMaxDamage(15))
)