package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.Viewport
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL31

object ScaleSampling {
    val oneQuarterFramebuffer by lazy {
        val framebuffer = ScalingFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC,
            0.25
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        PostProcessRenderer.manageFramebuffer(framebuffer)
        framebuffer
    }

    val oneQuarterFramebuffer2 by lazy {
        val framebuffer = ScalingFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC,
            0.25
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        PostProcessRenderer.manageFramebuffer(framebuffer)
        framebuffer
    }

    val oneHalfFramebuffer by lazy {
        val framebuffer = ScalingFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC,
            0.5
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        PostProcessRenderer.manageFramebuffer(framebuffer)
        framebuffer
    }

    val oneHalfFramebuffer2 by lazy {
        val framebuffer = ScalingFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC,
            0.5
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        PostProcessRenderer.manageFramebuffer(framebuffer)
        framebuffer
    }

    val framebuffer = PostProcessRenderer.createManagedFramebuffer()
    val framebuffer2 = PostProcessRenderer.createManagedFramebuffer()

    private var sourceFramebuffer: Framebuffer? = null
    private var targetFramebuffer: Framebuffer? = null

    private val sourceFramebufferProvider = UniformProvider("framebuffer") { pointer ->
        val sourceFramebuffer = sourceFramebuffer ?: return@UniformProvider

        GL31.glActiveTexture(GlConst.GL_TEXTURE0)
        GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sourceFramebuffer.colorAttachment)
        RenderSystem.glUniform1i(pointer, 0)
    }

    private val sourceResolutionProvider = UniformProvider("sourceResolution") { pointer ->
        val framebuffer = sourceFramebuffer ?: return@UniformProvider

        GL31.glUniform2f(pointer, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat())
    }

    private val targetResolutionProvider = UniformProvider("targetResolution") { pointer ->
        val framebuffer = targetFramebuffer ?: return@UniformProvider

        GL31.glUniform2f(pointer, framebuffer.textureWidth.toFloat(), framebuffer.textureHeight.toFloat())
    }

    // Bi-linear sampling method
    val bilinearSample by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/sampling/bilinear.fsh"),
            arrayOf(sourceFramebufferProvider, sourceResolutionProvider, targetResolutionProvider)
        )
    }

    fun sample(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer, sampler: BlitShader) {
        val previousFramebuffer = GlStateManager.getBoundFramebuffer()
        val previousViewportX = Viewport.getX()
        val previousViewportY = Viewport.getY()
        val previousViewportWidth = Viewport.getWidth()
        val previousViewportHeight = Viewport.getHeight()

        this.sourceFramebuffer = sourceFramebuffer
        this.targetFramebuffer = targetFramebuffer

        targetFramebuffer.beginWrite(true)
        sampler.blit()

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
        GlStateManager._viewport(previousViewportX, previousViewportY, previousViewportWidth, previousViewportHeight)
    }
}