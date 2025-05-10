package heckerpowered.matrix.client.render.particle

import heckerpowered.matrix.client.render.particle.module.ParticleRenderModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.ParticleSpawnModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleUpdateModule

class ParticleSystem(
    particleCount: Int,
    val particleSpawnModules: Array<ParticleSpawnModule> = emptyArray(),
    val particleUpdateModules: Array<ParticleUpdateModule> = emptyArray(),
    val particleRenderModules: Array<ParticleRenderModule> = emptyArray(),
) {
    val particleStates = GpuParticleState.createGpuParticleState(particleCount)

    init {
        spawnParticles()
    }

    fun spawnParticles() {
        for (particleSpawnModule in particleSpawnModules) {
            particleSpawnModule.run(particleStates)
        }
    }

    fun spawnPartialParticles(first: Int, count: Int) {
        for (particleSpawnModule in particleSpawnModules) {
            particleSpawnModule.run(particleStates, first, count)
        }
    }

    fun spawnPartialParticles(range: IntRange) {
        spawnPartialParticles(range.first, range.count())
    }

    fun updateParticles() {
        for (particleUpdateModule in particleUpdateModules) {
            particleUpdateModule.run(particleStates)
        }
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