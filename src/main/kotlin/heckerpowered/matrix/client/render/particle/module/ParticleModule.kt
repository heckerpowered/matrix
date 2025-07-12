package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.particle.ParticleState
import heckerpowered.matrix.client.shader.component.TransformFeedback
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL40.GL_TRANSFORM_FEEDBACK
import org.lwjgl.opengl.GL40.glBindTransformFeedback

abstract class ParticleModule {
    companion object {
        val TRANSFORM_FEEDBACK = TransformFeedback(
            varyingNames = arrayOf(
                "OutPosition", "OutVelocity", "OutAcceleration",
                "OutSpriteSize", "OutScale", "OutAge", "OutLifetime",
                "OutColor", "OutOrientation", "OutAngularVelocity",
            ),
            initOnly = true
        )
    }

    protected open fun disableRasterizer() {
        glEnable(GL_RASTERIZER_DISCARD)
    }

    protected open fun enableRasterizer() {
        glDisable(GL_RASTERIZER_DISCARD)
    }

    open fun bind(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount) {
        particleStates.bind()
        disableRasterizer()

        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0)

        val offset = first * ParticleState.BYTES.toLong()
        val size = count * ParticleState.BYTES.toLong()
        glBindBufferRange(GL_TRANSFORM_FEEDBACK_BUFFER, 0, particleStates.vertexBufferObjectPong, offset, size)

        glBeginTransformFeedback(GL_POINTS)
    }

    open fun dispatchCompute(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount) {
        glDrawArrays(GL_POINTS, first, count)
    }

    open fun unbind(particleStates: GpuParticleState) {
        glEndTransformFeedback()

        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0)
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0)

        enableRasterizer()
        particleStates.unbind()
        particleStates.swapBuffers()
    }

    open fun run(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount) {
        bind(particleStates, first, count)
        dispatchCompute(particleStates, first, count)
        unbind(particleStates)
    }
}