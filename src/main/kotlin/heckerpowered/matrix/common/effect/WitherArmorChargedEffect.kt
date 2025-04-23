package heckerpowered.matrix.common.effect

import heckerpowered.matrix.client.player
import heckerpowered.matrix.common.enchantment.witherArmorEnchantmentKey
import heckerpowered.matrix.common.event.*
import heckerpowered.matrix.common.network.SyncHealthPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult

object WitherArmorChargedEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x32C8A8
) {
    init {
        fadeTicks(0)
        StatusEffectRemovedCallback.EVENT.register(::onStatusEffectRemoved)
        LivingDamageCallback.EVENT.register(::onLivingDamage)
        EntityTickCallback.EVENT.register(::onEntityTick)
        LivingDeathCallback.EVENT.register(::onLivingDeath)
    }

    private fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
        val attacker = damageSource.attacker
        if (attacker !is ServerPlayerEntity || !attacker.bloodPactActive) {
            return ActionResult.PASS
        }
        val witherArmorEnchantment = attacker.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(witherArmorEnchantmentKey)
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, attacker.getEquippedStack(EquipmentSlot.CHEST))
        if (level <= 0) {
            return ActionResult.PASS
        }

        val statusEffect = attacker.getStatusEffect(witherArmorChargedEffect)
        val amplifier = statusEffect?.amplifier ?: 0
        val duration = statusEffect?.duration ?: 200
        attacker.setStatusEffect(StatusEffectInstance(witherArmorChargedEffect, duration, amplifier + 1, false, true), entity)

        return ActionResult.PASS
    }

    fun onEntityTick(entity: LivingEntity) {
        val witherArmorEnchantment = entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(witherArmorEnchantmentKey)
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, entity.getEquippedStack(EquipmentSlot.CHEST))
        if (level <= 0) {
            return
        }

        val witherArmorChargedStatusEffectInstance = entity.getStatusEffect(witherArmorChargedEffect) ?: return
        val amplifier = witherArmorChargedStatusEffectInstance.amplifier
        if (amplifier <= 0) {
            return
        }

        if (entity.health > entity.maxHealth * 0.5) {
            return
        }

        val healAmount = 1F + level * 1F

        // Heal may exceed the maximum health, convert it to absorption.
        if (entity.health + healAmount > entity.maxHealth) {
            // Absorption amount gains by wither armor has a maximum limit, make sure it does not exceed the
            // maximum absorption amount, and does not less than the current absorption amount, do not call
            // .coerceIn() because the size relationship of two values are unknown.
            val maxAbsorptionAmount = entity.maxHealth
            val newAbsorptionAmount = (entity.absorptionAmount + entity.health + healAmount - entity.maxHealth)
                .coerceAtMost(maxAbsorptionAmount)
                .coerceAtLeast(player.absorptionAmount)
            entity.setAbsorptionAmountUnclamped(newAbsorptionAmount)
        }
        entity.heal(healAmount)
        entity.setStatusEffect(StatusEffectInstance(witherArmorEffect, 200, level - 1, false, true).also {
            // Cannot add a weaker status effect to an entity, when we set the status effect directly,
            // it is not considered as a new status effect, call onApplied() manually.
            it.onApplied(entity)
        }, entity)

        // Wither armor charges has no special effect when it is applied. Call onApplied() is not necessary.
        entity.setStatusEffect(
            StatusEffectInstance(witherArmorChargedEffect, 200, amplifier - 1, false, true),
            entity
        )
        if (entity is ServerPlayerEntity) {
            entity.serverWorld.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 3.0F, 1.0F)

            // When the wither armor is triggered when the time is slowed down, the health and absorption amount
            // may not be synchronized to the client just in time. Send a packet to synchronize the health and
            // absorption amount to the client.
            ServerPlayNetworking.send(entity, SyncHealthPayload(entity))
            // ServerPlayNetworking.send(entity, WitherArmorTriggerPayload())
        }
    }

    private fun onStatusEffectRemoved(entity: LivingEntity, statusEffectInstance: StatusEffectInstance): ActionResult {
        // Status removed callback may be called in client, remember to check if the entity is client-side.
        if (entity.world.isClient ||
            statusEffectInstance.effectType != witherArmorChargedEffect ||
            statusEffectInstance.duration > 0
        ) {
            return ActionResult.PASS
        }

        val witherArmorEnchantment = entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(witherArmorEnchantmentKey)
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, entity.getEquippedStack(EquipmentSlot.CHEST))
        if (level <= 0) {
            return ActionResult.PASS
        }

        // Avoiding the next amplifier being less than the current, this restriction may result in a reduction
        // of wither armor charges when the player obtains more than maximum number of charges by other ways.
        // (e.g. commands).
        val currentAmplifier = statusEffectInstance.amplifier
        val nextAmplifier = (currentAmplifier + 1)
            .coerceAtMost(3)
            .coerceAtLeast(currentAmplifier)
        entity.addStatusEffect(StatusEffectInstance(witherArmorChargedEffect, 200, nextAmplifier, false, true))
        return ActionResult.FAIL
    }

    private fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        val entity = event.entity
        val amount = event.amount
        if (entity.world.isClient) {
            return ActionResult.PASS
        }

        val registryManager = entity.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val witherArmorEnchantment = registryWrapper.getOrThrow(witherArmorEnchantmentKey)
        val equippedChestplate = entity.getEquippedStack(EquipmentSlot.CHEST)
        val level = EnchantmentHelper.getLevel(witherArmorEnchantment, equippedChestplate)
        if (level <= 0) {
            return ActionResult.PASS
        }

        val witherArmorChargedStatusEffectInstance = entity.getStatusEffect(witherArmorChargedEffect) ?: return ActionResult.PASS
        val amplifier = witherArmorChargedStatusEffectInstance.amplifier
        if (amplifier <= 0) {
            return ActionResult.PASS
        }

        // Effect trigger condition
        if (entity.health > entity.maxHealth * 0.5 &&
            entity.health + entity.absorptionAmount - amount > entity.maxHealth * 0.5
        ) {
            return ActionResult.PASS
        }

        // Calculate how many times the wither armor needs to be used to save the owner.
        val healAmount = 1F + level * 1F
        val absorptionAmount = entity.maxHealth * (0.05F + level * 0.05F)
        val useAmount = ((amount - entity.health) / (healAmount + absorptionAmount)).toInt().coerceIn(0..<amplifier)

        val neutralizedDamageAmount = (healAmount + absorptionAmount) * useAmount
        event.amount -= neutralizedDamageAmount

        // Heal may exceed the maximum health, convert it to absorption.
        if (entity.health + healAmount > entity.maxHealth) {
            // Absorption amount gains by wither armor has a maximum limit, make sure it does not exceed the
            // maximum absorption amount, and does not less than the current absorption amount, do not call
            // .coerceIn() because the size relationship of two values are unknown.
            val maxAbsorptionAmount = entity.maxHealth
            val newAbsorptionAmount = (entity.absorptionAmount + entity.health + healAmount - entity.maxHealth)
                .coerceAtMost(maxAbsorptionAmount)
                .coerceAtLeast(player.absorptionAmount)
            entity.setAbsorptionAmountUnclamped(newAbsorptionAmount)
        }
        entity.heal(healAmount)
        entity.setStatusEffect(StatusEffectInstance(witherArmorEffect, 200, level - 1, false, true).also {
            // Cannot add a weaker status effect to an entity, when we set the status effect directly,
            // it is not considered as a new status effect, call onApplied() manually.
            it.onApplied(entity)
        }, entity)

        // Wither armor charges has no special effect when it is applied. Call onApplied() is not necessary.
        entity.setStatusEffect(
            StatusEffectInstance(witherArmorChargedEffect, 200, amplifier - useAmount - 1, false, true),
            entity
        )
        if (entity is ServerPlayerEntity) {
            entity.serverWorld.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 3.0F, 1.0F)

            // When the wither armor is triggered when the time is slowed down, the health and absorption amount
            // may not be synchronized to the client just in time. Send a packet to synchronize the health and
            // absorption amount to the client.
            ServerPlayNetworking.send(entity, SyncHealthPayload(entity))
            // ServerPlayNetworking.send(entity, WitherArmorTriggerPayload())
        }
        return ActionResult.PASS
    }
}