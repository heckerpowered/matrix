package heckerpowered.matrix.client.render

import org.lwjgl.opengl.GL46.*

class AdvancedFramebuffer(width: Int, height: Int, val useDepthAttachment: Boolean = false, val mipmapLevels: Int = recommendMipLevel(width, height)) {

    var colorAttachment: Int = -1
        private set

    var depthAttachment: Int = -1
        private set

    var framebufferObject: Int = -1
        private set

    init {
        checkTextureSize()
    }

    private fun initFramebuffer() {
        framebufferObject = glGenFramebuffers()
        colorAttachment = glGenTextures()
        if (useDepthAttachment) {
            depthAttachment = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, depthAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        }
    }

    private fun checkTextureSize() {
        val maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        1..<maxTextureSize
        // if (textureWidth !in textureSizeRange ||
        //     textureHeight !in textureSizeRange
        // ) {
        //     throw IllegalArgumentException("Window ${textureWidth}x$textureHeight size out of bounds (max. size: $maxTextureSize")
        // }
    }

    fun resize(width: Int, height: Int) {

    }
}