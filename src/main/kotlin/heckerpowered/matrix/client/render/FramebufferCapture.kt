package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager

object FramebufferCapture {
    private var previousFramebuffer: Int = 0

    val captureFramebuffer = PostProcessRenderer.createManagedFramebuffer()

    fun beginCapture() {
        if (previousFramebuffer != 0) {
            throw IllegalStateException("Cannot begin capture while another capture is in progress")
        }
        previousFramebuffer = GlStateManager.getBoundFramebuffer()
        captureFramebuffer.beginWrite(false)
    }

    fun endCapture() {
        captureFramebuffer.endWrite()
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
        previousFramebuffer = 0
    }
}