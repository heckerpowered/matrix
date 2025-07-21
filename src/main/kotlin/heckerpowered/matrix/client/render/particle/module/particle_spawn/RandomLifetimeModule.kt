package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL20.glUniform1f

class RandomLifetimeModule : ParticleSpawnModule() {
    companion object {
        var minLifetime = 0.8F
        var maxLifetime = 1.75F

        private val shader = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_spawn/random_lifetime.vsh"),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                UniformProvider("MinLifetime") { pointer -> glUniform1f(pointer, minLifetime) },
                UniformProvider("MaxLifetime") { pointer -> glUniform1f(pointer, maxLifetime) }
            ),
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int) {
        shader.enableShader()
        super.bind(particleStates, first, count)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        shader.disableShader()
    }
}
