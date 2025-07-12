package heckerpowered.matrix.core

import com.mojang.blaze3d.platform.GlStateManager
import heckerpowered.matrix.client.render.OpenGLExtensions.clearGLError
import heckerpowered.matrix.client.render.recommendMipLevel
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL46.*

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

        /**
         * Indicates whether mipmaps should be allocated for this [Framebuffer].
         *
         * When set to `true`, the framebuffer's color attachment will be initialized
         * with enough storage to hold a full mipmap chain, allowing rendering to or sampling
         * from individual mipmap levels. This is particularly useful for effects such as
         * bloom, where lower-resolution versions of the framebuffer are needed.
         *
         * After changing this property, the framebuffer must be re-initialized
         * to allocate storage for the mipmap chain. Mipmap storage cannot be allocated during construction.
         *
         * This property relies on Mixin. If the Mixin is not properly initialized,
         * accessing this property will always return `false`, and setting it will have no effect.
         * No exceptions will be thrown in such cases.
         */
        var Framebuffer.allocateMipmaps: Boolean
            get() = (this as? FramebufferExtension)?.useMipmaps ?: false
            set(value) {
                (this as? FramebufferExtension)?.useMipmaps = value
            }

        /**
         *
         */
        fun Framebuffer.beginWriteLod(mipLevel: Int, attachment: Int = GL_COLOR_ATTACHMENT0, setTextureSize: Boolean = true, setViewport: Boolean = true) {
            clearGLError()

            beginWrite(false)
            glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, colorAttachment, mipLevel)
            endWrite()

            beginRead()
            val textureWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, mipLevel, GL_TEXTURE_WIDTH)
            val textureHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, mipLevel, GL_TEXTURE_HEIGHT)
            endRead()

            this.textureWidth = textureWidth
            this.textureHeight = textureHeight

            if (setViewport) {
                viewportWidth = textureWidth
                viewportHeight = textureHeight
                GlStateManager._viewport(0, 0, textureWidth, textureHeight)
            }
        }

        fun Framebuffer.endWriteLod() {
            beginWriteLod(0)
        }

        fun Framebuffer.beginReadLod(mipLevel: Int) {
            beginRead()
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, mipLevel)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, mipLevel)
            endRead()
        }

        fun Framebuffer.endReadLod() {
            beginRead()
            val textureWidth = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)
            val textureHeight = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)

            val maxMipLevel = recommendMipLevel(textureWidth, textureHeight)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, maxMipLevel)
            endRead()
        }
    }

    var useMipmaps: Boolean
}