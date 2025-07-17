package heckerpowered.matrix.client.render.state

import org.lwjgl.opengl.GL46

class ViewportState(val viewportX: Int, val viewportY: Int, val viewportWidth: Int, val viewportHeight: Int) : RenderPipelineState {
    companion object {
        fun captureSnapshot(): ViewportState {
            val viewportState = IntArray(4)
            GL46.glGetIntegerv(GL46.GL_VIEWPORT, viewportState)

            val snapshot = ViewportState(
                viewportX = viewportState[0],
                viewportY = viewportState[1],
                viewportWidth = viewportState[2],
                viewportHeight = viewportState[3]
            )
            return snapshot
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot()

        GL46.glViewport(viewportX, viewportY, viewportWidth, viewportHeight)

        return RenderPipelineSnapshot(snapshot)
    }
}