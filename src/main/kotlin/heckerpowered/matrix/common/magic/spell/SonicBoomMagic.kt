/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.magic.spell

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.common.magic.ChannelQueue
import heckerpowered.matrix.common.magic.ExecutionPayload
import heckerpowered.matrix.common.magic.GameTick.Companion.ticks
import heckerpowered.matrix.common.magic.Magic
import heckerpowered.matrix.common.magic.MagicDefinition
import heckerpowered.matrix.common.magic.Mana.Companion.mana
import heckerpowered.matrix.common.tag.MatrixDamageTypes
import heckerpowered.matrix.core.extensions.EntityExtensions.damage
import heckerpowered.matrix.core.utility.EntitySearch.getNearestEntities
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.floor

object SonicBoomMagic : Magic(
    MagicDefinition(
        Matrix.identifier("sonic_boom"),
        40.mana,
        34.ticks
    )
) {
    override fun cast(player: ServerPlayerEntity?, target: LivingEntity, sequence: ChannelQueue, data: ExecutionPayload) {
        super.cast(player, target, sequence, data)
        if (player == null) {
            return
        }

        val world = player.serverWorld
        castSonicBoom(world, player, target)
        target.getNearestEntities(20.0)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .take(5)
            .forEach {
                castSonicBoom(world, player, it, target.pos) { dist -> dist.coerceAtLeast(20) }
            }
    }

    private fun castSonicBoom(world: ServerWorld, attacker: LivingEntity, target: LivingEntity, startPosition: Vec3d = attacker.eyePos, distanceModifier: (Int) -> Int = { it }) {
        val startPosition = startPosition
        val endPosition = target.eyePos
        val direction = endPosition.subtract(startPosition)
        val normalizedDirection = direction.normalize()

        val step = distanceModifier(floor(direction.length()).toInt() + 7)
        for (i in 1..step) {
            val currentPosition = startPosition.add(normalizedDirection.multiply(i.toDouble()))
            world.spawnParticles(ParticleTypes.SONIC_BOOM, currentPosition.x, currentPosition.y, currentPosition.z, 1, 0.0, 0.0, 0.0, 0.0)

            val boundingBox = Box(currentPosition, currentPosition).expand(3.0)
            for (entity in world.getOtherEntities(attacker, boundingBox)) {
                if (entity.damage(world.damageSources.create(MatrixDamageTypes.magic, attacker), 10.0f)) {
                    0.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                    2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
                    // entity.addVelocity(normalizedDirection.x * verticalKnockback, normalizedDirection.y * horizontalKnockback, normalizedDirection.z * verticalKnockback)
                }
            }
        }

        world.playSound(null, attacker.x, attacker.y, attacker.z, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 3.0F, 1.0F)
        if (target.damage(10.0f, world.damageSources.sonicBoom(attacker))) {
            0.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            2.5 * (1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE))
            // target.addVelocity(normalizedDirection.x * verticalKnockback, normalizedDirection.y * horizontalKnockback, normalizedDirection.z * verticalKnockback)
        }
    }
}