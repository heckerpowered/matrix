package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.effect.witherArmorChargedEffect
import heckerpowered.matrix.common.event.EntityTickCallback
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity

object WitherArmorEnchantment {
    fun onInitialize() {
        EntityTickCallback.event.register(::onEntityTick)
    }

    private fun onEntityTick(entity: LivingEntity) {
        if (entity.world.isClient) {
            return
        }

        val itemStack = entity.getEquippedStack(EquipmentSlot.CHEST)
        val witherArmorEnchantment = entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(witherArmorEnchantmentKey)
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, itemStack)
        if (level <= 0) {
            return
        }

        val witherArmorChargedStatusEffectInstance = entity.getStatusEffect(witherArmorChargedEffect)
        if (witherArmorChargedStatusEffectInstance != null) {
            return
        }

        entity.addStatusEffect(StatusEffectInstance(witherArmorChargedEffect, 20 * 10, 0, true, true))
        if (entity is ServerPlayerEntity) {
            entity.server.playerManager.sendStatusEffects(entity)
        }
    }
}