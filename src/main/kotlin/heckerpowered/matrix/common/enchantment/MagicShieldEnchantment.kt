package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.MAGIC_SHIELD_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.common.event.GetArmorCallback
import heckerpowered.matrix.core.Accumulator
import heckerpowered.matrix.core.inverseLerp
import heckerpowered.matrix.core.mana
import heckerpowered.matrix.core.maxMana
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry

object MagicShieldEnchantment {
    fun onInitialize() {
        GetArmorCallback.EVENT.register(::getArmor)
        AccumulateAttributeValueCallback.EVENT.register(::getAttributeValue)
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute != EntityAttributes.GENERIC_ARMOR_TOUGHNESS || entity !is PlayerEntity) {
            return
        }

        adjustArmorAndThoughness(entity, accumulator)
    }

    private fun getArmor(entity: LivingEntity, accumulator: Accumulator) {
        if (entity !is PlayerEntity) {
            return
        }

        adjustArmorAndThoughness(entity, accumulator)
    }

    private fun adjustArmorAndThoughness(entity: PlayerEntity, accumulator: Accumulator) {
        val registryManager = entity.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val magicShieldEnchantmentEntry = registryWrapper.getOrThrow(MAGIC_SHIELD_ENCHANTMENT_KEY)
        val equippedHelmet = entity.getEquippedStack(EquipmentSlot.HEAD)
        val magicShieldLevel = EnchantmentHelper.getLevel(magicShieldEnchantmentEntry, equippedHelmet)
        if (magicShieldLevel <= 0) {
            return
        }

        val mana = entity.mana
        val maxMana = entity.maxMana
        val percentage = mana.inverseLerp((maxMana * 0.5)..maxMana).coerceIn(.0..1.0)
        if (percentage.isNaN() || percentage.isInfinite()) {
            return
        }
        accumulator.multiplier += percentage * 0.2 * magicShieldLevel
    }
}