/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageSettlementContext
import heckerpowered.matrix.common.combat.damage.DamageSettlementRule
import heckerpowered.matrix.common.enchantment.ModEnchantments.WitherArmor
import heckerpowered.matrix.common.enchantment.getEnchantmentLevel
import heckerpowered.matrix.common.entity.rule.*
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.rule.effect.ChannelEffect
import heckerpowered.matrix.common.network.ClientboundSyncHealthPayload
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.addAbsorptionUpTo
import heckerpowered.matrix.core.extension.healMeasured
import heckerpowered.matrix.core.extension.healOverflow
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.ceil

object WitherArmorChargedEffect : MobEffect(
    MobEffectCategory.BENEFICIAL,
    0x32C8A8
), EffectRemovedRule, EntityUpdateRule, LivingDeathRule, DamageSettlementRule, ChannelEffect {
    init {
        setBlendDuration(0)
        RuleRegistry.register<EffectRemovedRule>(this)
        RuleRegistry.register<EntityUpdateRule>(this)
        RuleRegistry.register<LivingDeathRule>(this)
        RuleRegistry.register<ChannelEffect>(this)
        RuleRegistry.register<DamageSettlementRule>(this)
    }

    override fun onRemoved(context: EffectRemovedContext) {
        val entity = context.entity
        val effectInstance = context.effectInstance
        val effect = effectInstance.effect

        // Status removed callback may be called in client, remember to check if the entity is client-side.
        if (entity.level().isClientSide) return
        if (effect != ModMobEffects.WitherArmorCharged) return
        if (effectInstance.duration > 0) return

        val level = entity.getEnchantmentLevel(WitherArmor).takeIf { it > 0 } ?: return

        // Avoiding the next amplifier being less than the current, this restriction may result in a reduction
        // of wither armor charges when the player obtains more than maximum number of charges by other ways.
        // (e.g. commands).
        val currentAmplifier = effectInstance.amplifier
        val maxAmplifier = level.coerceAtMost(3)
        val nextAmplifier = (currentAmplifier + 1)
            .coerceAtMost(maxAmplifier)
            .coerceAtLeast(currentAmplifier)
        entity.addEffect(MobEffectInstance(ModMobEffects.WitherArmorCharged, 200, nextAmplifier, false, true))
    }

    override fun onUpdate(context: EntityUpdateContext) {
        val entity = context.entity as? LivingEntity ?: return
        val context = buildContext(entity) ?: return
        if (!shouldTriggerOnTick(context)) return

        trigger(context, 0)
    }

    override fun onLivingDeath(context: LivingDeathContext) {
        val attacker = context.damageSource.entity
        if (attacker !is ServerPlayer || !attacker.isBloodPactActive) return
        if (attacker.getEnchantmentLevel(WitherArmor) <= 0) return

        val statusEffect = attacker.getEffect(ModMobEffects.WitherArmorCharged)
        val amplifier = statusEffect?.amplifier ?: 0
        val duration = statusEffect?.duration ?: 200
        attacker.addEffect(MobEffectInstance(ModMobEffects.WitherArmorCharged, duration, amplifier + 1, false, true), attacker)
    }

    override fun onChannel(magic: Magic, invocation: MagicInvocation) {
        val caster = invocation.caster.entityOrNull() as? Player ?: return
        if (!caster.isBloodPactActive) return
        onUpdate(EntityUpdateContext(caster))
    }

    override fun onSettlement(context: DamageSettlementContext) {
        val entity = context.target
        val amount = context.remainingDamage
        if (entity.level().isClientSide) return

        val calculationContext = buildContext(entity) ?: return
        if (!shouldTriggerOnDamage(calculationContext, amount)) return

        val extraCharges = calculationContext.computeExtraChargesForDamage(amount)
        if (extraCharges > 0) {
            context.consume(calculationContext.recoveryAmountPerCharge * extraCharges)
        }
        trigger(calculationContext, extraCharges)
    }

    private fun buildContext(entity: LivingEntity): CalculationContext? {
        val level = entity.getEnchantmentLevel(WitherArmor).takeIf { it > 0 } ?: return null
        val charges = entity.getEffect(ModMobEffects.WitherArmorCharged)?.amplifier ?: 0
        if (charges <= 0) return null
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

        entity.healOverflow(context.healAmountPerCharge)
        applyHealWithOverflow(entity, context.healAmountPerCharge)
        applyWitherArmorEffect(entity, context.level)

        // Wither armor charges has no special effect when it is applied. Call onApplied() is not necessary.
        entity.forceAddEffect(
            MobEffectInstance(ModMobEffects.WitherArmorCharged, 200, remainingCharges, false, true),
            entity
        )
        notifyTriggered(entity)
    }

    private fun applyHealWithOverflow(entity: LivingEntity, healAmount: Float) {
        val healMeasurement = entity.healMeasured(healAmount)
        entity.addAbsorptionUpTo(healMeasurement.overflowAmount, entity.maxHealth)
    }

    private fun applyWitherArmorEffect(entity: LivingEntity, level: Int) {
        entity.forceAddEffect(MobEffectInstance(ModMobEffects.WitherArmorCharged, 200, level - 1, false, true).also {
            // Cannot add a weaker status effect to an entity, when we set the status effect directly,
            // it is not considered as a new status effect, call onApplied() manually.
            it.onEffectStarted(entity)
        }, entity)
    }

    private fun notifyTriggered(entity: LivingEntity) {
        if (entity is ServerPlayer) {
            entity.level().playSound(null, entity.x, entity.y, entity.z, SoundEvents.WITHER_BREAK_BLOCK, SoundSource.PLAYERS, 3.0F, 1.0F)

            // When the wither armor is triggered when the time is slowed down, the health and absorption amount
            // may not be synchronized to the client just in time. Send a packet to synchronize the health and
            // absorption amount to the client.
            ServerPlayNetworking.send(entity, ClientboundSyncHealthPayload(entity))
            // ServerPlayNetworking.send(entity, WitherArmorTriggerPayload())
        }
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
