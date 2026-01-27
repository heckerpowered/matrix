/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.effect.MatrixStatusEffects.ABSOLVRIFT_EFFECT
import heckerpowered.matrix.common.effect.MatrixStatusEffects.HEALTH_SHRINK_EFFECT
import heckerpowered.matrix.common.event.EntityTickCallback
import heckerpowered.matrix.common.event.LivingDamageCallback
import heckerpowered.matrix.common.event.LivingDamageEvent
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.magic.Mana.Companion.plus
import heckerpowered.matrix.common.persistent.mana
import heckerpowered.matrix.common.persistent.maxMana
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import heckerpowered.matrix.core.extensions.LivingEntityExtensions.attackDamage
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.extensions.SequenceExtensions.drain
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult


object AbsolvriftEffect : StatusEffect(
    StatusEffectCategory.BENEFICIAL,
    0x6A00FF
) {
    init {
        EntityTickCallback.EVENT.register(::onEntityTick)
        LivingDamageCallback.EVENT.register(::onLivingDamage)
    }

    fun onLivingDamage(event: LivingDamageEvent): ActionResult {
        if (event.damageSource.isOf(MatrixDamageTypes.recursiveMagic)) {
            return ActionResult.PASS
        }

        val attacker = event.damageSource.attacker
        if (attacker !is LivingEntity || !attacker.hasStatusEffect(ABSOLVRIFT_EFFECT)) {
            return ActionResult.PASS
        }

        val damageSource = attacker.world.damageSources.create(MatrixDamageTypes.recursiveMagic, attacker)
        val entity = event.entity
        if (entity.damage(attacker.attackDamage.toFloat() * 0.756F, damageSource)) {
            val server = entity.world.server ?: return ActionResult.PASS
            val statusEffectInstance = entity.getStatusEffect(HEALTH_SHRINK_EFFECT) ?: return ActionResult.PASS
            for (serverPlayer in server.playerManager.playerList) {
                serverPlayer.networkHandler.sendPacket(EntityStatusEffectS2CPacket(entity.id, statusEffectInstance, false))
            }

            if (entity is PlayerEntity) {
                entity.addCritParticles(entity)
                entity.addEnchantedHitParticles(entity)
            }
            if (entity is ServerPlayerEntity) {
                entity.mana += (entity.maxMana.amount * 0.001).mana
            }
        }

        return ActionResult.PASS
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
                        entity.mana += (entity.maxMana.amount * 0.01).mana
                    }
                }

                result
            }.drain()
    }
}