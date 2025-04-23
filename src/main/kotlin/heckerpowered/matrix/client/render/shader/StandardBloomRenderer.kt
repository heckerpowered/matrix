package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.dump
import heckerpowered.matrix.client.render.recommendMipLevel
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL46.*

object StandardBloomRenderer {
    private var brightnessPass: Int = -1
    private val framebuffer: Framebuffer by lazy {
        val framebuffer = PostProcessRenderer.createManagedFramebuffer()
        // (framebuffer as FramebufferExtension).useMipmaps = true
        // glBindTexture(GL_TEXTURE_2D, framebuffer.colorAttachment)
        // glGenerateMipmap(GL_TEXTURE_2D)
        framebuffer
    }

    fun render(brightnessPass: Framebuffer) {
        // this.brightnessPass = brightnessPass.fbo
        // val colorAttachment = this.brightnessPass
//
        // glBindTexture(GL_TEXTURE_2D, colorAttachment)
        // glGenerateMipmap(GL_TEXTURE_2D)
//
        // val boundFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING)
        // val boundColorAttachment = glGetFramebufferParameteri(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0)
//
        // glBindTexture(GL_TEXTURE_2D, boundColorAttachment)

        // Render to mipmaps
        framebuffer.beginWrite(true)
        glBindTexture(GL_TEXTURE_2D, framebuffer.colorAttachment)
        glTexStorage2D(GL_TEXTURE_2D, framebuffer.recommendMipLevel(), GL_RGBA8, framebuffer.textureWidth, framebuffer.textureHeight)

        glFramebufferTexture2D(
            GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
            GL_TEXTURE_2D, framebuffer.colorAttachment, 1
        )

        brightnessPass.draw(brightnessPass.textureWidth, brightnessPass.textureHeight)
        framebuffer.dump(1, false)

        minecraft.framebuffer.beginWrite(false)
    }
}