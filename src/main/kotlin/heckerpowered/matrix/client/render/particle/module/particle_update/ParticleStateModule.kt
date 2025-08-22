package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import org.lwjgl.opengl.GL46

class ParticleStateModule() : ParticleUpdateModule() {
    companion object {
        private val Program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_update/particle_state.vsh", GL46.GL_VERTEX_SHADER),
            uniforms = arrayOf(DELTA_TIME_PROVIDER),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        Program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        Program.disableShader()
    }
}