/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.effect

import heckerpowered.matrix.common.combat.damage.*
import heckerpowered.matrix.common.effect.MatrixStatusEffects.IGNITE_EFFECT
import heckerpowered.matrix.common.event.AccumulateAttributeValueCallback
import heckerpowered.matrix.common.event.GetArmorCallback
import heckerpowered.matrix.common.rule.RuleRegistry
import heckerpowered.matrix.common.rule.register
import heckerpowered.matrix.core.Accumulator
import heckerpowered.matrix.core.extension.EntityExtension.damage
import heckerpowered.matrix.core.extension.isAdditionalDamage
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object IgniteEffect : StatusEffect(
    StatusEffectCategory.HARMFUL,
    0xD9471D
), DamageComputationRule, DamageOutcomeRule {
    init {
        GetArmorCallback.EVENT.register(::getArmor)
        AccumulateAttributeValueCallback.EVENT.register(::getAttributeValue)
        RuleRegistry.register<DamageComputationRule>(this)
        RuleRegistry.register<DamageOutcomeRule>(this)
    }

    private fun getAttributeValue(entity: LivingEntity, attribute: RegistryEntry<EntityAttribute>, accumulator: Accumulator) {
        if (attribute == EntityAttributes.GENERIC_ARMOR_TOUGHNESS && entity.hasStatusEffect(IGNITE_EFFECT)) {
            accumulator.multiplier -= 0.4
        }
    }

    private fun getArmor(entity: LivingEntity, accumulator: Accumulator) {
        if (entity.hasStatusEffect(IGNITE_EFFECT)) {
            accumulator.multiplier -= 0.4
        }
    }

    private fun randomUnitVector(): Vec3d {
        val theta = Random.nextDouble() * Math.PI * 2
        val phi = acos(2 * Random.nextDouble() - 1)

        val x = sin(phi) * cos(theta)
        val y = sin(phi) * sin(theta)
        val z = cos(phi)

        return Vec3d(x, y, z)
    }

    private fun spawnIgniteFlameEffect(target: LivingEntity) {
        val serverWorld = target.world as? ServerWorld ?: return

        val particleCount = 40
        val speed = 0.4

        repeat(particleCount) {
            val direction = randomUnitVector()

            serverWorld.spawnParticles(
                ParticleTypes.FLAME,
                target.x,
                target.y + target.height * 0.5,
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
        if (!target.hasStatusEffect(IGNITE_EFFECT)) return
        if (!source.isIn(DamageTypeTags.IS_FIRE)) return

        context.damageMultiplier += 0.2
    }

    override fun onOutcome(context: DamageOutcomeContext) {
        val source = context.source
        val target = context.target

        if (context.source.isAdditionalDamage) return
        val igniteEffect = target.getStatusEffect(IGNITE_EFFECT) ?: return
        if (source.isIn(DamageTypeTags.IS_FIRE)) return

        igniteEffect.mapDuration { it + 10 }
        target.fireTicks += 10

        val damageSource = target.world.damageSources.create(DamageTypes.ON_FIRE, context.attacker)
        damageSource.isAdditionalDamage = true
        target.damage(context.rawDamage * 0.2F, damageSource)
        spawnIgniteFlameEffect(target)
    }
}