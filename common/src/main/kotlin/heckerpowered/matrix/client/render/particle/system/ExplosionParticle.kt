/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.system

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.particle.ParticleSystem
import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.particle.module.particle_render.ParticleSpriteRendererModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.InitializeParticleModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomLifetimeModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.RandomVelocityModule
import heckerpowered.matrix.client.render.particle.module.particle_update.DragModule
import heckerpowered.matrix.client.render.particle.module.particle_update.KillParticleModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleStateModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ScaleSpriteSizeBySpeedModule
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.random.Random

object ExplosionParticle {
    val randomVelocityModule = RandomVelocityModule()

    init {
        randomVelocityModule.multiplier = Vector3f(10.0F)
    }

    val particleSystem by lazy {
        ParticleSystem(
            10000,
            particleSpawnModules = arrayOf(
                InitializeParticleModule(),
                randomVelocityModule,
                RandomLifetimeModule(),
            ),
            particleUpdateModules = arrayOf(
                KillParticleModule(),
                ParticleStateModule(),
                DragModule(),
                ScaleSpriteSizeBySpeedModule()
            ),
            particleRenderModules = arrayOf(
                ParticleSpriteRendererModule()
            ),
            MemoryLayout.DEFAULT_LAYOUT
        )
    }

    fun spawnParticleAt(position: Vec3) {
        spawnVanillaFallback(position)

        val particleState = (particleSystem.particleSpawnModules.first { it is InitializeParticleModule } as InitializeParticleModule).particleState
        particleState.x = position.x.toFloat()
        particleState.y = position.y.toFloat()
        particleState.z = position.z.toFloat()

        val multiplier = 4F

        particleState.colorR = 0.1F * multiplier
        particleState.colorG = 0.5F * multiplier
        particleState.colorB = 1.0F * multiplier
        particleState.colorA = 1.0F

        particleState.spriteSize = 80.0F
        particleState.scale = 1F

        particleSystem.spawnParticles()
    }

    private fun spawnVanillaFallback(position: Vec3) {
        val level = minecraft.level ?: return
        repeat(80) {
            val velocity = Vec3(
                (Random.nextDouble() - 0.5) * 0.7,
                Random.nextDouble() * 0.7,
                (Random.nextDouble() - 0.5) * 0.7,
            )
            level.addParticle(
                ParticleTypes.END_ROD,
                position.x,
                position.y,
                position.z,
                velocity.x,
                velocity.y,
                velocity.z,
            )
        }
    }
}
