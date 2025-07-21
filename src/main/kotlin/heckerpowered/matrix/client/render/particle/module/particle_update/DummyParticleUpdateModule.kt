package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.module.DummyParticleModule
import heckerpowered.matrix.client.render.state.StateIsolation

class DummyParticleUpdateModule : ParticleUpdateModule() {
    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        DummyParticleModule.shader.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        DummyParticleModule.shader.disableShader()
    }
}