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
    private val scaledFramebuffers = mutableMapOf<Double, ScalingFramebuffer>()
    fun getScaledFramebuffer(scaling: Double): ScalingFramebuffer {
        return scaledFramebuffers.computeIfAbsent(scaling) {
            val framebuffer = ScalingFramebuffer(
                minecraft.window.framebufferWidth,
                minecraft.window.framebufferHeight,
                true,
                MinecraftClient.IS_SYSTEM_MAC,
                scaling
            )
            framebuffer.setClearColor(.0F, .0F, .0F, .0F)
            PostProcessRenderer.manageFramebuffer(framebuffer)
            framebuffer
        }
    }

    val framebuffer = PostProcessRenderer.createManagedFramebuffer()

    var levelOfDetail = 0F

    private var sourceFramebuffer: Framebuffer? = null
    private var targetFramebuffer: Framebuffer? = null

    private val sourceFramebufferProvider = UniformProvider("framebuffer") { pointer ->
        val sourceFramebuffer = sourceFramebuffer ?: return@UniformProvider

        GL31.glActiveTexture(GlConst.GL_TEXTURE0)
        GL31.glBindTexture(GlConst.GL_TEXTURE_2D, sourceFramebuffer.colorAttachment)

        GL31.glGenerateMipmap(GL31.GL_TEXTURE_2D)
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR_MIPMAP_LINEAR)
        GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR)

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

    val textureLod by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/lower_sampling/lod.fsh"),
            arrayOf(
                sourceFramebufferProvider,
                UniformProvider("levelOfDetail") { pointer ->
                    GL31.glUniform1f(pointer, levelOfDetail)
                }
            )
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