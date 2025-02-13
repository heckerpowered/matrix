package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object WoodenLeggingsItem : ArmorItem(
    woodenArmorMaterial,
    Type.LEGGINGS,
    Settings()
        .maxDamage(Type.LEGGINGS.getMaxDamage(5))
)