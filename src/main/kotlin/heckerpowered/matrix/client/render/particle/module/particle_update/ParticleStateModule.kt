package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.core.resourceToString

class ParticleStateModule() : ParticleUpdateModule() {
    companion object {
        private val SHADER = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_update/particle_state.vsh"),
            uniforms = arrayOf(DELTA_TIME_PROVIDER),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int) {
        SHADER.enableShader()
        super.bind(particleStates, first, count)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        SHADER.disableShader()
    }
}