package heckerpowered.matrix.client.render.particle.module.particle_render

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.module.ParticleRenderModule
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL46.*

class ParticleSpriteRendererModule : ParticleRenderModule() {
    companion object {
        private val SHADER = Shader(
            resourceToString("/assets/matrix/shaders/particle/particle_render/point_sprite_renderer.vsh"),
            resourceToString("/assets/matrix/shaders/particle/particle_render/point_sprite_renderer.fsh"),
            uniforms = arrayOf(
                PROJECTION_MATRIX_PROVIDER,
                MODEL_VIEW_MATRIX_PROVIDER
            ),
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        glEnable(GL_PROGRAM_POINT_SIZE)
        SHADER.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        SHADER.disableShader()
        glDisable(GL_PROGRAM_POINT_SIZE)
    }
}