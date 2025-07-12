package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.core.resourceToString

/**
 * Dummy particle module shader, does nothing, used for testing purposes.
 */
object DummyParticleModule : ParticleModule() {
    val shader = Shader(
        resourceToString("/assets/matrix/shaders/particle/template.glsl"),
        components = arrayOf(TRANSFORM_FEEDBACK)
    )

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int) {
        shader.enableShader()
        super.bind(particleStates, first, count)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        shader.disableShader()
    }
}