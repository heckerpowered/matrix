/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.enchantment.MatrixEnchantments.LIGHTNING_STRIKE_ENCHANTMENT_KEY
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.EntityExtension.damage
import heckerpowered.matrix.core.extension.SequenceExtension.consumeWhile
import heckerpowered.matrix.core.extension.isAdditionalDamage
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.utility.EntitySearch.getAdjacentEntities
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import kotlin.math.floor

/**
 *
 */
object LightningStrikeEnchantment : DamageOutcomeRule {
    fun onInitialize() {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        if (context.source.isAdditionalDamage) {
            return
        }
        val attacker = context.attackerAsLiving() ?: return
        val serverWorld = attacker.world as? ServerWorld ?: return

        val registryManager = attacker.world.registryManager
        val registryWrapper = registryManager.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
        val enchantmentEntry = registryWrapper.getOrThrow(LIGHTNING_STRIKE_ENCHANTMENT_KEY)
        val enchantmentLevel = attacker.handItems.sumOf { EnchantmentHelper.getLevel(enchantmentEntry, it) }
        if (enchantmentLevel <= 0) return

        val damageSource = context.source
        damageSource.isAdditionalDamage = true
        var previousEntity = attacker
        context.target.getAdjacentEntities(8.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != attacker && it != context.target }
            .consumeWhile(5) {
                val result = it.damage(context.rawDamage * 0.2F, damageSource)
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
    }
}
