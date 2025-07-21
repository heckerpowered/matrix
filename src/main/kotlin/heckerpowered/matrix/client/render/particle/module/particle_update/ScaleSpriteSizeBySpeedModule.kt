package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL20.glUniform1f

class ScaleSpriteSizeBySpeedModule : ParticleUpdateModule() {
    companion object {
        var minScaleFactor = 0.0F
        var maxScaleFactor = 1.0F
        var velocityThreshold = 1.0F

        private val SHADER = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_update/scale_sprite_size_by_speed.vsh"),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                UniformProvider("MinScaleFactor") { pointer -> glUniform1f(pointer, minScaleFactor) },
                UniformProvider("MaxScaleFactor") { pointer -> glUniform1f(pointer, maxScaleFactor) },
                UniformProvider("VelocityThreshold") { pointer -> glUniform1f(pointer, velocityThreshold) }
            )
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