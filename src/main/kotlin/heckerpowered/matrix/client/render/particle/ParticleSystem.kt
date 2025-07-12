package heckerpowered.matrix.client.render.particle

import heckerpowered.matrix.client.render.particle.module.ParticleRenderModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.ParticleSpawnModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleUpdateModule
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

class ParticleSystem(
    val particleCount: Int,
    val particleSpawnModules: Array<ParticleSpawnModule> = emptyArray(),
    val particleUpdateModules: Array<ParticleUpdateModule> = emptyArray(),
    val particleRenderModules: Array<ParticleRenderModule> = emptyArray(),
    var lifeTime: Duration = Duration.INFINITE,
    var maxLifeTime: Duration = Duration.INFINITE,
) {
    private var lastFrameTime: Duration = Duration.ZERO
    val particleStates = GpuParticleState.createGpuParticleState(particleCount)

    fun spawnParticles() {
        for (particleSpawnModule in particleSpawnModules) {
            particleSpawnModule.run(particleStates)
        }
        lastFrameTime = System.nanoTime().nanoseconds
    }

    fun spawnPartialParticles(first: Int, count: Int) {
        for (particleSpawnModule in particleSpawnModules) {
            particleSpawnModule.run(particleStates, first, count)
        }
        lastFrameTime = System.nanoTime().nanoseconds
    }

    fun spawnPartialParticles(range: IntRange) {
        spawnPartialParticles(range.first, range.count())
    }

    fun updateParticles(deltaTime: Duration = (System.nanoTime().nanoseconds - lastFrameTime)) {
        for (particleUpdateModule in particleUpdateModules) {
            particleUpdateModule.run(particleStates)
        }
        lifeTime += deltaTime
        lastFrameTime = System.nanoTime().nanoseconds
    }

    fun updatePartialParticles(first: Int, count: Int) {
        for (particleUpdateModules in particleUpdateModules) {
            particleUpdateModules.run(particleStates, first, count)
        }
    }

    fun updatePartialParticles(range: IntRange) {
        updatePartialParticles(range.first, range.count())
    }

    fun renderParticles() {
        for (particleRenderModules in particleRenderModules) {
            particleRenderModules.run(particleStates)
        }
    }

    fun renderPartialParticles(first: Int, count: Int) {
        for (particleRenderModules in particleRenderModules) {
            particleRenderModules.run(particleStates, first, count)
        }
    }

    fun renderPartialParticles(range: IntRange) {
        renderPartialParticles(range.first, range.count())
    }
}
