/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.system

import heckerpowered.matrix.client.render.MatrixGraphicsBackend
import heckerpowered.matrix.client.render.MatrixPointSpriteParticles
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
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

object ExplosionParticle {
    private const val ENABLE_LEGACY_OPENGL_PARTICLES = false

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
        MatrixPointSpriteParticles.spawnExplosionParticles(
            position,
            randomVelocityModule.speedRange,
            randomVelocityModule.multiplier,
        )
        if (!ENABLE_LEGACY_OPENGL_PARTICLES || !MatrixGraphicsBackend.isOpenGl()) {
            return
        }

        // The legacy GPU particle path is OpenGL transform-feedback based. It is
        // kept for OpenGL only until the point-sprite renderer is fully rebuilt
        // on Minecraft's cross-backend RenderPipeline API.
        runCatching {
            spawnLegacyGpuParticles(position)
        }
    }

    private fun spawnLegacyGpuParticles(position: Vec3) {
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
}
