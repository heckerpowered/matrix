package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL30.*

class FramebufferState(val framebuffer: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): FramebufferState {
            val previousBindingFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING)
            val snapshot = FramebufferState(previousBindingFramebuffer)
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBindFramebuffer(GL_FRAMEBUFFER, framebuffer)

        return RenderPipelineSnapshot(snapshot)
    }
}