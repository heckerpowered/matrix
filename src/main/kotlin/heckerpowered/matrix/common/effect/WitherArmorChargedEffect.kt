package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.common.event.LivingHurtEvent
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
        StatusEffectRemovedCallback.event.register(::onStatusEffectRemoved)
        LivingHurtCallback.event.register(::onLivingHurt)
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

    private fun onLivingHurt(event: LivingHurtEvent): ActionResult {
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

        if (entity.health - amount > entity.maxHealth * 0.5) {
            return ActionResult.PASS
        }
        entity.heal(1F + level * 1F)
        entity.setStatusEffect(StatusEffectInstance(witherArmorEffect, 200, level - 1, false, true).also {
            it.onApplied(entity)
        }, entity)
        entity.setStatusEffect(StatusEffectInstance(witherArmorChargedEffect, 200, amplifier - 1, false, true), entity)
        return ActionResult.PASS
    }
}