package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL46

class RandomVelocityModule : ParticleSpawnModule() {
    companion object {
        private val program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_spawn/random_velocity.vsh", GL46.GL_VERTEX_SHADER),
            uniforms = arrayOf(UniformProvider("time") { pointer ->
                glUniform1f(pointer, (System.currentTimeMillis() % 10000) / 1000F)
            }),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        program.disableShader()
    }
}
