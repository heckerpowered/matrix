package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL46

class BlendFuncState(val srcFactor: Int, val dstFactor: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): BlendFuncState {
            val previousSrcFactor = GL46.glGetInteger(GL46.GL_BLEND_SRC_RGB)
            val previousDstFactor = GL46.glGetInteger(GL46.GL_BLEND_DST_RGB)
            val snapshot = BlendFuncState(previousSrcFactor, previousDstFactor)
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        glBlendFunc(srcFactor, dstFactor)

        return RenderPipelineSnapshot(snapshot)
    }
}