package heckerpowered.matrix.common.item

import net.minecraft.item.ArmorItem

object EmeraldChestplateItem : ArmorItem(
    emeraldArmorMaterial,
    Type.CHESTPLATE,
    Settings()
        .maxDamage(Type.CHESTPLATE.getMaxDamage(35))
)