package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL20.glUniform1f

class RandomVelocityModule : ParticleSpawnModule() {
    companion object {
        private val shader = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_spawn/random_velocity.vsh"),
            uniforms = arrayOf(UniformProvider("time") { pointer ->
                glUniform1f(pointer, (System.currentTimeMillis() % 10000) / 1000F)
            }),
            components = arrayOf(TRANSFORM_FEEDBACK)
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
