/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageSettlementContext
import heckerpowered.matrix.common.combat.damage.DamageSettlementRule
import heckerpowered.matrix.common.effect.MatrixStatusEffects.WITHER_ARMOR_CHARGED_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.WITHER_ARMOR_EFFECT
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.WITHER_ARMOR_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.event.LivingDeathCallback
import heckerpowered.matrix.common.event.StatusEffectRemovedCallback
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.network.SyncHealthPayload
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import kotlin.math.ceil

object WitherArmorChargedEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x32C8A8
), DamageSettlementRule, ChannelEffect {
    init {
        fadeTicks(0)
        StatusEffectRemovedCallback.EVENT.register(::onStatusEffectRemoved)
        EntityTickCallback.EVENT.register(::onEntityTick)
        LivingDeathCallback.EVENT.register(::onLivingDeath)
        RuleRegistry.register<ChannelEffect>(this)
        RuleRegistry.register<DamageSettlementRule>(this)
    }

    private fun onLivingDeath(entity: LivingEntity, damageSource: DamageSource): ActionResult {
        val attacker = damageSource.attacker
        if (attacker !is ServerPlayerEntity || !attacker.isBloodPactActive) {
            return ActionResult.PASS
        }
        val level = getWitherArmorLevel(attacker)
        if (level <= 0) {
            return ActionResult.PASS
        }

        val statusEffect = attacker.getStatusEffect(WITHER_ARMOR_CHARGED_EFFECT)
        val amplifier = statusEffect?.amplifier ?: 0
        val duration = statusEffect?.duration ?: 200
        attacker.setStatusEffect(StatusEffectInstance(WITHER_ARMOR_CHARGED_EFFECT, duration, amplifier + 1, false, true), entity)

        return ActionResult.PASS
    }

    fun onEntityTick(entity: LivingEntity) {
        val context = buildContext(entity) ?: return
        if (!shouldTriggerOnTick(context)) return

        trigger(context, 0)
    }

    private fun onStatusEffectRemoved(entity: LivingEntity, statusEffectInstance: StatusEffectInstance): ActionResult {
        // Status removed callback may be called in client, remember to check if the entity is client-side.
        if (entity.world.isClient ||
            statusEffectInstance.effectType != WITHER_ARMOR_CHARGED_EFFECT ||
            statusEffectInstance.duration > 0
        ) {
            return ActionResult.PASS
        }

        val level = getWitherArmorLevel(entity)
        if (level <= 0) {
            return ActionResult.PASS
        }

        // Avoiding the next amplifier being less than the current, this restriction may result in a reduction
        // of wither armor charges when the player obtains more than maximum number of charges by other ways.
        // (e.g. commands).
        val currentAmplifier = statusEffectInstance.amplifier
        val maxAmplifier = level.coerceAtMost(3)
        val nextAmplifier = (currentAmplifier + 1)
            .coerceAtMost(maxAmplifier)
            .coerceAtLeast(currentAmplifier)
        entity.addStatusEffect(StatusEffectInstance(WITHER_ARMOR_CHARGED_EFFECT, 200, nextAmplifier, false, true))
        return ActionResult.FAIL
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.entityOrNull() as PlayerEntity
        if (!caster.isBloodPactActive) return
        onEntityTick(caster)
    }

    override fun onSettlement(context: DamageSettlementContext) {
        val entity = context.target
        val amount = context.remainingDamage
        if (entity.world.isClient) return

        val calculationContext = buildContext(entity) ?: return
        if (!shouldTriggerOnDamage(calculationContext, amount)) return

        val extraCharges = calculationContext.computeExtraChargesForDamage(amount)
        if (extraCharges > 0) {
            context.consume(calculationContext.recoveryAmountPerCharge * extraCharges)
        }
        trigger(calculationContext, extraCharges)
    }

    private fun buildContext(entity: LivingEntity): CalculationContext? {
        val level = getWitherArmorLevel(entity)
        if (level <= 0) {
            return null
        }
        val charges = entity.getStatusEffect(WITHER_ARMOR_CHARGED_EFFECT)?.amplifier ?: 0
        if (charges <= 0) {
            return null
        }
        return CalculationContext(entity, level, charges)
    }

    private fun shouldTriggerOnTick(context: CalculationContext): Boolean {
        return context.entity.health <= context.entity.maxHealth * 0.5f
    }

    private fun shouldTriggerOnDamage(context: CalculationContext, damage: Float): Boolean {
        val entity = context.entity
        return entity.health <= entity.maxHealth * 0.5f ||
                entity.health + entity.absorptionAmount - damage <= entity.maxHealth * 0.5f
    }

    private fun trigger(context: CalculationContext, extraCharges: Int) {
        val entity = context.entity
        val totalChargesUsed = 1 + extraCharges
        val remainingCharges = (context.charges - totalChargesUsed).coerceAtLeast(0)

        applyHealWithOverflow(entity, context.healAmountPerCharge)
        applyWitherArmorEffect(entity, context.level)

        // Wither armor charges has no special effect when it is applied. Call onApplied() is not necessary.
        entity.setStatusEffect(
            StatusEffectInstance(WITHER_ARMOR_CHARGED_EFFECT, 200, remainingCharges, false, true),
            entity
        )
        notifyTriggered(entity)
    }

    private fun applyHealWithOverflow(entity: LivingEntity, healAmount: Float) {
        if (entity.health + healAmount > entity.maxHealth) {
            // Absorption amount gains by wither armor has a maximum limit, make sure it does not exceed the
            // maximum absorption amount, and does not less than the current absorption amount, do not call
            // .coerceIn() because the size relationship of two values are unknown.
            val maxAbsorptionAmount = entity.maxHealth
            val newAbsorptionAmount = (entity.absorptionAmount + entity.health + healAmount - entity.maxHealth)
                .coerceAtMost(maxAbsorptionAmount)
                .coerceAtLeast(entity.absorptionAmount)
            entity.setAbsorptionAmountUnclamped(newAbsorptionAmount)
        }
        entity.heal(healAmount)
    }

    private fun applyWitherArmorEffect(entity: LivingEntity, level: Int) {
        entity.setStatusEffect(StatusEffectInstance(WITHER_ARMOR_EFFECT, 200, level - 1, false, true).also {
            // Cannot add a weaker status effect to an entity, when we set the status effect directly,
            // it is not considered as a new status effect, call onApplied() manually.
            it.onApplied(entity)
        }, entity)
    }

    private fun notifyTriggered(entity: LivingEntity) {
        if (entity is ServerPlayerEntity) {
            entity.serverWorld.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, SoundCategory.PLAYERS, 3.0F, 1.0F)

            // When the wither armor is triggered when the time is slowed down, the health and absorption amount
            // may not be synchronized to the client just in time. Send a packet to synchronize the health and
            // absorption amount to the client.
            ServerPlayNetworking.send(entity, SyncHealthPayload(entity))
            // ServerPlayNetworking.send(entity, WitherArmorTriggerPayload())
        }
    }

    private fun getWitherArmorLevel(entity: LivingEntity): Int {
        val witherArmorEnchantment = entity.world.registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(WITHER_ARMOR_ENCHANTMENT_KEY)
        return EnchantmentHelper.getLevel(witherArmorEnchantment, entity.getEquippedStack(EquipmentSlot.CHEST))
    }

    private data class CalculationContext(
        val entity: LivingEntity,
        val level: Int,
        val charges: Int,
    ) {
        val healAmountPerCharge = 1f + level * 1f
        val shieldAmountPerCharge = entity.maxHealth * (0.05f + level * 0.05f)
        val recoveryAmountPerCharge = healAmountPerCharge + shieldAmountPerCharge

        fun computeExtraChargesForDamage(damage: Float): Int {
            if (charges <= 1) {
                return 0
            }

            val remainingAfterCurrentBuffer = damage - entity.health - entity.absorptionAmount
            if (remainingAfterCurrentBuffer <= 0f) {
                return 0
            }

            val requiredExtra = ceil(remainingAfterCurrentBuffer / recoveryAmountPerCharge).toInt()
            return requiredExtra.coerceAtMost(charges - 1).coerceAtLeast(0)
        }
    }
}
