package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.Magic
import heckerpowered.matrix.data.language.key
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.MutableText

val witherArmorEnchantmentKey = of("wither_armor")
val guaranteedEnchantmentKey = of("guaranteed")
val lastStandEnchantmentKey = of("last_stand")
val revivalEnchantmentKey = of("revival")
val secondWindEnchantmentKey = of("second_wind")
val proximatePropagationEnchantmentKey = of("proximate_propagation")
val magicQueue = of("magic_queue")
val queueAcceleration = of("queue_acceleration")
val queueMastery = of("queue_mastery")
val manaOverflow = of("mana_overflow")
val manaRegeneration = of("mana_regeneration")
val wizardForce = of("wizard_force")
val bloodPact = of("blood_pact")
val magicShield = of("magic_shield")
val brutalStrength = of("brutal_strength")
val peakOverdrive = of("peak_overdrive")

fun LivingEntity.getEnchantmentLevel(registryKey: RegistryKey<Enchantment>): Int {
    val entry = world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(registryKey)
    return EnchantmentHelper.getEquipmentLevel(entry, this)
}

internal fun of(name: String): RegistryKey<Enchantment> {
    val identifier = Matrix.identifier(name)
    return RegistryKey.of(RegistryKeys.ENCHANTMENT, identifier)
}

val Magic.enchantmentKey: RegistryKey<Enchantment>
    get() {
        return of((this.name as MutableText).key.substringAfterLast('.'))
    }

object MatrixEnchantments {
    fun onInitialize() {
        WitherArmorEnchantment.onInitialize()
        GuaranteedEnchantment.onInitialize()
        LastStandEnchantment.onInitialize()
        RevivalEnchantment.onInitialize()
        SecondWindEnchantment.onInitialize()
        QueueMasteryEnchantment.onInitialize()
        WizardForceEnchantment.onInitialize()
        BrutalStrengthEnchantment.onInitialize()
        MagicShieldEnchantment.onInitialize()
        PeakOverdriveEnchantment.onInitialize()
    }
}

fun ItemStack.getEnchantmentLevel(registryKey: RegistryKey<Enchantment>): Int {
    val entry = enchantments.enchantments.filter { !it.key.isEmpty }.find { it.key.get() == registryKey }
    if (entry == null) {
        return -1
    }

    return EnchantmentHelper.getLevel(entry, this)
}