package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.module.DummyParticleModule

class DummyParticleSpawnModule : ParticleSpawnModule() {
    override fun bind(particleStates: GpuParticleState, first: Int, count: Int) {
        DummyParticleModule.shader.enableShader()
        super.bind(particleStates, first, count)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        DummyParticleModule.shader.disableShader()
    }
}