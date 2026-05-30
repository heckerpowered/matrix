/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.channel.MagicInvocation
import heckerpowered.matrix.common.magic.channel.entityOrNull
import heckerpowered.matrix.common.magic.core.Magic
import heckerpowered.matrix.common.magic.core.MagicDefinition
import heckerpowered.matrix.common.magic.resource.Mana.Companion.mana
import heckerpowered.matrix.common.magic.system.GameTick.Companion.ticks
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extension.damage
import heckerpowered.matrix.core.utility.getNearestEntities
import heckerpowered.matrix.core.utility.getOtherEntities
import heckerpowered.matrix.core.utility.withinDistance
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import kotlin.math.floor

object SonicBoomMagic : Magic(
    MagicDefinition(
        Matrix.identifier("sonic_boom"),
        40.mana,
        34.ticks
    )
) {
    override fun cast(invocation: MagicInvocation) {
        super.cast(invocation)

        val caster = invocation.caster.entityOrNull() ?: return
        val target = invocation.target
        val world = invocation.caster.level as? ServerLevel ?: return

        castSonicBoom(world, caster, target)
        target.getNearestEntities(20.0)
            .withinDistance(target, 20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != caster }
            .take(5)
            .forEach {
                castSonicBoom(world, caster, it, target.position()) { dist -> dist.coerceAtLeast(20) }
            }
    }

    private fun castSonicBoom(level: ServerLevel, attacker: LivingEntity, target: LivingEntity, startPosition: Vec3 = attacker.eyePosition, distanceModifier: (Int) -> Int = { it }) {
        val startPosition = startPosition
        val endPosition = target.eyePosition
        val direction = endPosition.subtract(startPosition)
        val normalizedDirection = direction.normalize()

        val step = distanceModifier(floor(direction.length()).toInt() + 7)
        val damageSource = level.damageSources().source(MatrixDamageTypes.magic, attacker)
        for (i in 1..step) {
            val currentPosition = startPosition.add(normalizedDirection.scale(i.toDouble()))
            level.sendParticles(ParticleTypes.SONIC_BOOM, true, true, currentPosition.x, currentPosition.y, currentPosition.z, 1, 0.0, 0.0, 0.0, 0.0)

            for (entity in attacker.getOtherEntities(3.0)) {
                if (entity.hurtServer(level, damageSource, 10.0f)) {
                    0.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))
                    2.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))
                    // entity.addVelocity(normalizedDirection.x * verticalKnockback, normalizedDirection.y * horizontalKnockback, normalizedDirection.z * verticalKnockback)
                }
            }
        }

        level.playSound(null, attacker.x, attacker.y, attacker.z, SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F)
        if (target.damage(10.0f, level.damageSources().sonicBoom(attacker))) {
            0.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))
            2.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE))
            // target.addVelocity(normalizedDirection.x * verticalKnockback, normalizedDirection.y * horizontalKnockback, normalizedDirection.z * verticalKnockback)
        }
    }
}