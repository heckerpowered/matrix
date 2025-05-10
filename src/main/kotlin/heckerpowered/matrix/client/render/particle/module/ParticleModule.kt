package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.shader.component.TransformFeedback
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*

abstract class ParticleModule {
    companion object {
        val PARTICLE_TRANSFORM_FEEDBACK = TransformFeedback(
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

    open fun bind(particleStates: GpuParticleState) {
        particleStates.bind()
        disableRasterizer()

        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, particleStates.vertexBufferObjectPong)

        glBeginTransformFeedback(GL_POINTS)
    }

    open fun dispatchCompute(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount) {
        glDrawArrays(GL_POINTS, first, count)
    }

    open fun unbind(particleStates: GpuParticleState) {
        glEndTransformFeedback()

        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0)

        enableRasterizer()
        particleStates.unbind()
        particleStates.swapBuffers()
    }

    open fun run(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount) {
        bind(particleStates)
        dispatchCompute(particleStates, first, count)
        unbind(particleStates)
    }
}