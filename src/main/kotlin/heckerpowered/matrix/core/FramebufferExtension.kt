package heckerpowered.matrix.core

import org.lwjgl.opengl.GL30.GL_RGBA16F

interface FramebufferExtension {
    companion object {
        @JvmStatic
        var framebufferColorFormat = GL_RGBA16F

        fun <T> changeColorFormat(colorFormat: Int, action: () -> T): T {
            val previousColorFormat = framebufferColorFormat
            framebufferColorFormat = colorFormat
            val result = action()
            framebufferColorFormat = previousColorFormat
            return result
        }
    }

    var useMipmaps: Boolean
}