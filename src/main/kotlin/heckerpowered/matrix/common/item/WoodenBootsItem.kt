package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object WoodenBootsItem : ArmorItem(
    woodenArmorMaterial,
    Type.BOOTS,
    Settings()
        .maxDamage(Type.BOOTS.getMaxDamage(5))
)