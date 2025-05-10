package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.UniformProvider
import org.joml.Matrix4f
import org.lwjgl.opengl.GL46
import org.lwjgl.system.MemoryUtil

abstract class ParticleRenderModule : ParticleModule() {
    companion object {
        private val BUFFER = MemoryUtil.memAllocFloat(16)

        val PROJECTION_MATRIX_PROVIDER = UniformProvider("ProjectionMatrix") { pointer ->
            BUFFER.position(0)

            val gameRenderer = minecraft.gameRenderer
            val tickDelta = minecraft.renderTickCounter.getTickDelta(true)
            val projectionMatrix = gameRenderer.getBasicProjectionMatrix(gameRenderer.getFov(gameRenderer.camera, tickDelta, false))
            projectionMatrix.get(BUFFER)

            GL46.glUniformMatrix4fv(pointer, false, BUFFER)
        }

        val MODEL_VIEW_MATRIX_PROVIDER = UniformProvider("ModelViewMatrix") { pointer ->
            BUFFER.position(0)

            val camera = minecraft.gameRenderer.camera
            val viewMatrix = Matrix4f().apply {
                identity()

                camera.rotation.conjugate().get(this)

                translate(
                    (-camera.pos.x).toFloat(),
                    (-camera.pos.y).toFloat(),
                    (-camera.pos.z).toFloat()
                )
            }

            viewMatrix.get(BUFFER)

            GL46.glUniformMatrix4fv(pointer, false, BUFFER)
        }
    }

    override fun bind(particleStates: GpuParticleState) {
        enableRasterizer()
        particleStates.bind()
    }

    override fun unbind(particleStates: GpuParticleState) {
        particleStates.unbind()
    }
}