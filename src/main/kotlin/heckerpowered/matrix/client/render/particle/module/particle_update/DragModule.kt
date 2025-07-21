package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL20.glUniform1f

class DragModule : ParticleUpdateModule() {
    companion object {
        var minDrag = 0.8F
        var maxDrag = 1.2F

        private val SHADER = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_update/drag.vsh"),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                DELTA_TIME_PROVIDER,
                UniformProvider("MinDrag") { pointer -> glUniform1f(pointer, minDrag) },
                UniformProvider("MaxDrag") { pointer -> glUniform1f(pointer, maxDrag) }
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