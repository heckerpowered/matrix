package heckerpowered.matrix.client.render.state.capabilities

import org.lwjgl.opengl.GL11.GL_BLEND

class BlendState(enabled: Boolean) : CapabilityState(GL_BLEND, enabled) {
    companion object {
        fun captureSnapshot(): BlendState {
            val previousBlendState = glIsEnabled(GL_BLEND)
            val snapshot = BlendState(previousBlendState)
            return snapshot
        }
    }
}

