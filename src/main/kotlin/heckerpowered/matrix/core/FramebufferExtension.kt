package heckerpowered.matrix.core

import org.lwjgl.opengl.GL11.GL_RGBA8

interface FramebufferExtension {
    companion object {
        @JvmStatic
        var framebufferColorFormat = GL_RGBA8

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