/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.effect.MatrixStatusEffects.ABSOLVRIFT_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.HEALTH_SHRINK_EFFECT
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.resource.Mana.Companion.plus
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extension.EntityExtension.damage
import heckerpowered.matrix.core.extension.LivingEntityExtension.attackDamage
import heckerpowered.matrix.core.extension.SequenceExtension.consumeWhile
import heckerpowered.matrix.core.extension.SequenceExtension.drain
import heckerpowered.matrix.core.extension.isAdditionalDamage
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity


object AbsolvriftEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x6A00FF
), DamageOutcomeRule {
    init {
        EntityTickCallback.EVENT.register(::onEntityTick)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        if (context.source.isAdditionalDamage) return
        val attacker = context.source.attacker as? LivingEntity ?: return
        if (!attacker.hasStatusEffect(ABSOLVRIFT_EFFECT)) return

        val damageSource = attacker.world.damageSources.create(MatrixDamageTypes.magic, attacker)
        damageSource.isAdditionalDamage = true

        val target = context.target
        if (target.damage(attacker.attackDamage.toFloat() * 0.756F, damageSource)) {
            val server = target.world.server ?: return
            val statusEffectInstance = target.getStatusEffect(HEALTH_SHRINK_EFFECT) ?: return
            for (serverPlayer in server.playerManager.playerList) {
                serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(target.id, statusEffectInstance, false))
            }

            if (target is PlayerEntity) {
                target.addCritParticles(target)
                target.addEnchantedHitParticles(target)
            }
            if (target is ServerPlayerEntity) {
                target.mana += (target.maxMana.toDouble() * 0.001).mana
            }
        }

        return
    }

    fun onEntityTick(entity: LivingEntity) {
        if (!entity.hasStatusEffect(ABSOLVRIFT_EFFECT)) {
            return
        }
        if (entity.age % 20 != 0) {
            return
        }
        entity.getNearestEntities(6.0)
            .filterIsInstance<LivingEntity>()
            .sortedBy { it.distanceTo(entity) }
            .consumeWhile(5) {
                val damageSource = entity.world.damageSources.create(MatrixDamageTypes.magic, entity)
                val result = it.damage(entity.attackDamage.toFloat() * 0.2F, damageSource)
                if (result) {
                    val amplifier = it.getStatusEffect(HEALTH_SHRINK_EFFECT)?.amplifier ?: 0
                    it.addStatusEffect(StatusEffectInstance(HEALTH_SHRINK_EFFECT, 20 * 8, amplifier + 1, false, false, false))

                    val server = it.world.server ?: return@consumeWhile true
                    val statusEffectInstance = it.getStatusEffect(HEALTH_SHRINK_EFFECT) ?: return@consumeWhile true
                    for (serverPlayer in server.playerManager.playerList) {
                        serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(it.id, statusEffectInstance, false))
                    }

                    if (entity is PlayerEntity) {
                        entity.addCritParticles(it)
                        entity.addEnchantedHitParticles(it)
                    }
                    if (entity is ServerPlayerEntity) {
                        entity.mana += (entity.maxMana.toDouble() * 0.01).mana
                    }
                }

                result
            }.drain()
    }
}