package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import org.lwjgl.opengl.GL46

/**
 * Dummy particle module shader, does nothing, used for testing purposes.
 */
object DummyParticleModule : ParticleModule() {
    val program = Program(
        ResourceShader("/assets/matrix/shaders/particle/template.glsl", GL46.GL_VERTEX_SHADER),
        components = arrayOf(TRANSFORM_FEEDBACK)
    )

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        program.disableShader()
    }
}