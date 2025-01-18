package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.core.FramebufferSpoof
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderPhase
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20

object UIBlurShader : BlitShader(
    resourceToString("/assets/matrix/shaders/sobel.vert"),
    resourceToString("/assets/matrix/shaders/blur/ui_blur.frag"),
    arrayOf(
        UniformProvider("texture0") { pointer ->
            GlStateManager._activeTexture(GL13.GL_TEXTURE0)
            GlStateManager._bindTexture(minecraft.framebuffer.colorAttachment)
            GL20.glUniform1i(pointer, 0)
        },
        UniformProvider("overlay") { pointer ->
            val active = GlStateManager._getActiveTexture()
            GlStateManager._activeTexture(GL13.GL_TEXTURE9)
            GlStateManager._bindTexture(UIBlurShader.overlayFramebuffer.colorAttachment)
            GL20.glUniform1i(pointer, 9)
            GlStateManager._activeTexture(active)
        },
        UniformProvider("radius") { pointer -> GL20.glUniform1f(pointer, UIBlurShader.getBlurRadius()) }
    )) {
    private val overlayFramebuffer by lazy {
        val framebuffer = SimpleFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            true
        )

        framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        framebuffer
    }

    private fun getBlurRadius(): Float {
        return 20.0F
    }

    private var isDrawingHudFramebuffer = false

    val OUTLINE_TARGET = RenderPhase.Target("overlay_target", {
        if (isDrawingHudFramebuffer) {
            overlayFramebuffer.beginWrite(true)
        }
    }, {})

    @JvmStatic
    fun startUIOverlayDrawing(context: DrawContext, tickDelta: Float) {
        isDrawingHudFramebuffer = true

        overlayFramebuffer.clear(true)
        overlayFramebuffer.beginWrite(true)
        FramebufferSpoof.push(overlayFramebuffer)
    }

    @JvmStatic
    fun endUIOverlayDrawing() {
        if (!isDrawingHudFramebuffer) {
            return
        }

        isDrawingHudFramebuffer = false

        FramebufferSpoof.pop()
        overlayFramebuffer.endWrite()

        val projectionMatrix = RenderSystem.getProjectionMatrix()
        val vertexSorting = RenderSystem.getVertexSorting()

        RenderSystem.disableBlend()

        minecraft.framebuffer.beginWrite(true)

        // RenderSystem.setShaderColor(0F, 0F, 0F, 0F)
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA)

        overlayFramebuffer.drawInternal(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight, false)

        RenderSystem.setProjectionMatrix(projectionMatrix, vertexSorting)
        RenderSystem.defaultBlendFunc()
    }

    @JvmStatic
    fun setupDimensions(width: Int, height: Int) {
        this.overlayFramebuffer.resize(width, height, true)
    }
}
