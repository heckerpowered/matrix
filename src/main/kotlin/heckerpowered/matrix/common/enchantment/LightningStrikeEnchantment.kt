/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LIGHTNING_STRIKE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.event.DamageAccumulator
import heckerpowered.matrix.common.event.LivingHurtCallback
import heckerpowered.matrix.core.extensions.SequenceExtensions.consumeWhile
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.utility.EntitySearch.getAdjacentEntities
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import kotlin.math.floor

/**
 *
 */
object LightningStrikeEnchantment {
    fun onInitialize() {
        LivingHurtCallback.EVENT.register(::onHurt)
    }

    fun onHurt(event: DamageAccumulator): ActionResult {
        if (event.damageSource.isOf(DamageTypes.LIGHTNING_BOLT)) {
            return ActionResult.PASS
        }
        val attacker = event.attacker ?: return ActionResult.PASS
        val serverWorld = attacker.world as? ServerWorld ?: return ActionResult.PASS

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(LIGHTNING_STRIKE_ENCHANTMENT_KEY)
        val enchantmentLevel = attacker.handItems.sumOf { EnchantmentHelper.getLevel(enchantmentEntry, it) }
        if (enchantmentLevel <= 0) {
            return ActionResult.PASS
        }

        val damageSource = event.target.damageSources.create(DamageTypes.LIGHTNING_BOLT, attacker)
        var previousEntity = attacker
        event.target.getAdjacentEntities(8.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != attacker && it != event.target }
            .consumeWhile(5) {
                val result = it.damage(damageSource, event.baseDamage.toFloat() * 0.2F)
                return@consumeWhile result
            }
            .forEach { entity ->
                val startPosition = previousEntity.pos
                val endPosition = entity.pos
                val direction = endPosition - startPosition

                val step = floor(direction.length() * 10).toInt()
                val normalizedDirection = direction.normalize()
                for (i in 1..step) {
                    val currentPosition = startPosition.add(normalizedDirection.multiply(i.toDouble() * 0.1))
                    serverWorld.spawnParticles(ParticleTypes.SMOKE, currentPosition.x, currentPosition.y, currentPosition.z, 1, 0.0, 0.0, 0.0, 0.0)
                }

                previousEntity = entity
            }
        return ActionResult.PASS
    }
}