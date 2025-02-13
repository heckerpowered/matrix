package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.event.StatusEffectRemovedCallback
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.ActionResult

object WitherArmorChargedEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x32C8A8
) {
    init {
        fadeTicks(0)
        StatusEffectRemovedCallback.event.register(::onStatusEffectRemoved)
        LivingDamageCallback.event.register(::onLivingDamage)
    }

    private fun onStatusEffectRemoved(entity: LivingEntity, statusEffectInstance: StatusEffectInstance): ActionResult {
        if (entity.world.isClient ||
            statusEffectInstance.effectType != witherArmorChargedEffect ||
            statusEffectInstance.duration > 0
        ) {
            return ActionResult.PASS
        }

        val witherArmorEnchantment =
            entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(
                witherArmorEnchantmentKey
            )
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, entity.getEquippedStack(EquipmentSlot.CHEST))
        if (level <= 0) {
            return ActionResult.PASS
        }

        val nextAmplifier = (statusEffectInstance.amplifier + 1).coerceAtMost(3)
        entity.addStatusEffect(StatusEffectInstance(witherArmorChargedEffect, 200, nextAmplifier, false, true))
        return ActionResult.FAIL
    }

    private fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        val entity = event.entity
        val amount = event.amount

        val witherArmorEnchantment =
            entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(
                witherArmorEnchantmentKey
            )
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, entity.getEquippedStack(EquipmentSlot.CHEST))
        if (level <= 0) {
            return ActionResult.PASS
        }

        val witherArmorChargedStatusEffectInstance = entity.getStatusEffect(witherArmorChargedEffect)
            ?: return ActionResult.PASS
        val amplifier = witherArmorChargedStatusEffectInstance.amplifier
        if (amplifier <= 0) {
            return ActionResult.PASS
        }

        // Effect trigger condition
        if (entity.health + entity.absorptionAmount - amount > entity.maxHealth * 0.5) {
            return ActionResult.PASS
        }

        // Calculate how many times the wither armor needs to be used to save the owner.
        val healAmount = 1F + level * 1F
        val absorptionAmount = entity.maxHealth * (0.05F + level * 0.05F)
        val useAmount = ((amount - entity.health) / (healAmount + absorptionAmount)).toInt().coerceIn(0..<amplifier)

        val neutralizedDamageAmount = (healAmount + absorptionAmount) * useAmount
        event.amount -= neutralizedDamageAmount

        if (entity.health + healAmount > entity.maxHealth) {
            entity.absorptionAmount += (entity.health + healAmount) - entity.maxHealth
        }
        entity.heal(healAmount)
        entity.setStatusEffect(StatusEffectInstance(witherArmorEffect, 200, level - 1, false, true).also {
            it.onApplied(entity)
        }, entity)
        entity.setStatusEffect(
            StatusEffectInstance(witherArmorChargedEffect, 200, amplifier - useAmount - 1, false, true),
            entity
        )
        return ActionResult.SUCCESS
    }
}