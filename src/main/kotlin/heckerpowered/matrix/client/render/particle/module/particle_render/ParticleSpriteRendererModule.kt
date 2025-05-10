package heckerpowered.matrix.client.render.particle.module.particle_render

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.module.ParticleRenderModule
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

    override fun bind(particleStates: GpuParticleState) {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        glEnable(GL_PROGRAM_POINT_SIZE)
        SHADER.enableShader()
        super.bind(particleStates)
    }

    override fun unbind(particleStates: GpuParticleState) {
        super.unbind(particleStates)
        SHADER.disableShader()
        glDisable(GL_PROGRAM_POINT_SIZE)
        RenderSystem.disableBlend()
    }
}