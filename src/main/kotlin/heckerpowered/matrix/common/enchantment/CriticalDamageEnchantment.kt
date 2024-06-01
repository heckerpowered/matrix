package heckerpowered.matrix.common.enchantment

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot

class CriticalDamage :
    Enchantment(Rarity.VERY_RARE, EnchantmentTarget.WEAPON, arrayOf(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
    override fun getMinPower(level: Int): Int {
        return super.getMinPower(level) / 2
    }

    override fun getMaxLevel(): Int {
        return 10
    }
}