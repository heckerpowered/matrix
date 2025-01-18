package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.Matrix
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

val witherArmorEnchantmentKey = of("wither_armor")

internal fun of(name: String): RegistryKey<Enchantment> {
    val identifier = Matrix.identifier(name)
    return RegistryKey.of(RegistryKeys.ENCHANTMENT, identifier)
}

object MatrixEnchantments {
    fun onInitialize() {
        WitherArmorEnchantment.onInitialize()

        Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE
    }
}

fun ItemStack.getEnchantmentLevel(registryKey: RegistryKey<Enchantment>): Int {
    val entry = enchantments.enchantments.filter { !it.key.isEmpty }.find { it.key.get() == registryKey }
    if (entry == null) {
        return -1
    }

    return EnchantmentHelper.getLevel(entry, this)
}