/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.*
import heckerpowered.matrix.common.effect.ModMobEffects.IGNITE_EFFECT
import heckerpowered.matrix.common.entity.rule.AttributeComputationContext
import heckerpowered.matrix.common.entity.rule.AttributeComputationRule
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.extension.damage
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.DamageTypeTags
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object IgniteEffect : MobEffect(
    MobEffectCategory.HARMFUL,
    0xD9471D
), AttributeComputationRule, DamageComputationRule, DamageOutcomeRule {
    init {
        RuleRegistry.register<AttributeComputationRule>(this)
        RuleRegistry.register<DamageComputationRule>(this)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    override fun onComputation(context: AttributeComputationContext) {
        val entity = context.entity
        val attribute = context.attribute
        if (attribute == Attributes.ARMOR_TOUGHNESS && entity.hasEffect(ModMobEffects.Ignite)) {
            context.multiplier -= 0.4
        }
    }

    private fun randomUnitVector(): Vec3 {
        val theta = Random.nextDouble() * Math.PI * 2.0
        val z = Random.nextDouble() * 2.0 - 1.0
        val radius = sqrt(1.0 - z * z)

        val x = radius * cos(theta)
        val y = radius * sin(theta)

        return Vec3(x, y, z)
    }

    private fun spawnIgniteFlameEffect(target: LivingEntity) {
        val serverLevel = target.level() as? ServerLevel ?: return

        val particleCount = 40
        val speed = 0.4

        repeat(particleCount) {
            val direction = randomUnitVector()

            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                target.x,
                target.y + target.bbHeight * 0.5,
                target.z,
                0,
                direction.x * speed,
                direction.y * speed,
                direction.z * speed,
                1.0
            )
        }
    }

    override fun onComputation(context: DamageComputationContext) {
        val source = context.source
        val target = context.target

        if (context.source.isAdditionalDamage) return
        if (!target.hasEffect(IGNITE_EFFECT)) return
        if (!source.`is`(DamageTypeTags.IS_FIRE)) return

        context.damageMultiplier += 0.2
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val source = context.source
        val target = context.target

        if (context.source.isAdditionalDamage) return
        val igniteEffect = target.getEffect(IGNITE_EFFECT) ?: return
        if (source.`is`(DamageTypeTags.IS_FIRE)) return

        igniteEffect.mapDuration { it + 10 }
        target.remainingFireTicks += 10

        val damageSource = target.level().damageSources().source(DamageTypes.ON_FIRE, context.attacker)
        damageSource.isAdditionalDamage = true
        target.damage(context.rawDamage * 0.2F, damageSource)
        spawnIgniteFlameEffect(target)
    }
}