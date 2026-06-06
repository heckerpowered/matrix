/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.enchantment

import heckerpowered.matrix.common.combat.damage.DamageOutcomeContext
import heckerpowered.matrix.common.combat.damage.DamageOutcomeRule
import heckerpowered.matrix.common.combat.damage.attackerAsLiving
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.SequenceExtension.consumeWhile
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.core.minus
import heckerpowered.matrix.core.plus
import heckerpowered.matrix.core.times
import heckerpowered.matrix.core.utility.getAdjacentEntities
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import kotlin.math.floor

/**
 *
 */
object LightningStrikeEnchantment : DamageOutcomeRule {
    fun onInitialize() {
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        if (context.source.isAdditionalDamage) return
        val attacker = context.attackerAsLiving() ?: return
        val serverWorld = attacker.level() as? ServerLevel ?: return

        val enchantmentLevel = attacker.getEnchantmentLevel(ModEnchantments.LightningStrike)
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
                val startPosition = previousEntity.position()
                val endPosition = entity.position()
                val direction = endPosition - startPosition

                val step = floor(direction.length() * 10).toInt()
                val normalizedDirection = direction.normalize()
                for (i in 1..step) {
                    val currentPosition = startPosition + normalizedDirection * i.toDouble() * 0.1
                    serverWorld.sendParticles(ParticleTypes.SMOKE, currentPosition.x, currentPosition.y, currentPosition.z, 1, 0.0, 0.0, 0.0, 0.0)
                }

                previousEntity = entity
            }
    }
}
