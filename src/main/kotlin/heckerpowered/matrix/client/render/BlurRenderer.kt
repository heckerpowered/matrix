package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.core.FramebufferSpoof
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.gl.PostEffectProcessor
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL30


object BlurRenderer {
    private val blurProcessor = Identifier.ofVanilla("shaders/post/blur.json")

    val hudFramebuffer by lazy {
        val framebuffer = SimpleFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            true
        )

        framebuffer.setClearColor(0.0f, 0.0f, 0.0f, 255.0f)
        framebuffer
    }

    private val blurPostProcessor by lazy {
        PostEffectProcessor(
            minecraft.textureManager,
            minecraft.resourceManager,
            hudFramebuffer,
            blurProcessor
        ).apply {
            setupDimensions(minecraft.window.framebufferWidth, minecraft.window.framebufferHeight)
        }
    }

    private fun blitFramebuffer() {
        RenderSystem.assertOnRenderThread()
        val framebuffer = minecraft.framebuffer
        framebuffer.beginRead()
        hudFramebuffer.beginWrite(false)

        GL30.glBlitFramebuffer(
            0,
            0,
            framebuffer.textureWidth,
            framebuffer.textureHeight,
            0,
            0,
            hudFramebuffer.textureWidth,
            hudFramebuffer.textureHeight,
            GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT,
            GL30.GL_NEAREST
        )

        framebuffer.endRead()
        hudFramebuffer.endWrite()
    }

    fun renderBlur(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        blitFramebuffer()
        blurPostProcessor.setUniforms("Radius", 20.0F)
        blurPostProcessor.render(tickCounter.getTickDelta(false))
        // hudFramebuffer.beginWrite(false)

        FramebufferSpoof.push(hudFramebuffer)

        hudFramebuffer.beginWrite(false)
        hudFramebuffer.endWrite()

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()

        // Begin a triangle strip buffer using the POSITION_COLOR vertex format.
        val buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR)

        // Write our vertices, Z doesn't really matter since it's on the HUD.
        buffer.vertex(transformationMatrix, 20f, 20f, 5f).color(0).texture(0f, 1f)
        buffer.vertex(transformationMatrix, 5f, 40f, 5f).color(0).texture(0f, 1f)
        buffer.vertex(transformationMatrix, 35f, 40f, 5f).color(0).texture(0f, 1f)
        buffer.vertex(transformationMatrix, 20f, 60f, 5f).color(0).texture(0f, 1f)

        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderTexture(0, minecraft.framebuffer.colorAttachment)

        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        BufferRenderer.drawWithGlobalProgram(buffer.end())
    }
}