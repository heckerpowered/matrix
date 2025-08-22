package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.linearCopyTo
import heckerpowered.matrix.client.render.nearestCopyTo
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import heckerpowered.matrix.client.render.state.FramebufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.ViewportState
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import org.joml.Vector2f
import org.lwjgl.opengl.GL46.*
import java.io.File


object BlurRenderer {
    var initialFramebuffer = minecraft.framebuffer
    var currentFramebuffer = minecraft.framebuffer

    var radius = 5.0F
    var kawaseOffset = 1.0F
    var useDownscaling: Boolean = true

    private val imageProvider = UniformProvider("image") { pointer ->
        GlStateManager._activeTexture(GL_TEXTURE0)
        GlStateManager._bindTexture(currentFramebuffer.colorAttachment)
        glUniform1i(pointer, 0)
    }

    private val radiusProvider = UniformProvider("radius") { pointer ->
        glUniform1f(pointer, radius * MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat())
    }

    private val kawaseOffsetProvider = UniformProvider("offset") { pointer ->
        kawaseOffset += 3F
        val progress = MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat()
        glUniform2f(pointer, progress * kawaseOffset, progress * kawaseOffset)
    }

    private val halfResolutionBlurFramebuffer by lazy {
        ScaleSampling.createManagedScalingFramebuffer(0.5)
    }

    val blurFramebuffer by lazy {
        PostProcessRenderer.createManagedFramebuffer()
    }

    val horizontalBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/gaussian_blur_horizontal.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(PostProcessRenderer.framebufferProvider, radiusProvider)
    )

    val verticalBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/gaussian_blur_vertical.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(PostProcessRenderer.framebufferProvider, radiusProvider)
    )

    val kawaseBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/kawase_blur.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(PostProcessRenderer.framebufferProvider, kawaseOffsetProvider)
    )

    val tentBlurShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blur/tent.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(PostProcessRenderer.framebufferProvider)
    )

    private val colorfulShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/color/colorful.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            PostProcessRenderer.framebufferProvider,
            UniformProvider("brightness") { pointer ->
                glUniform1f(pointer, 1.1F)
            },
            UniformProvider("saturation") { pointer ->
                glUniform1f(pointer, 2.0F)
            },
            UniformProvider("contrast") { pointer ->
                glUniform1f(pointer, 1.0F)
            })
    )

    val blurTextureRenderProgram = Program(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/blur_mask.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(UniformProvider("image") { pointer ->
            GlStateManager._activeTexture(GL_TEXTURE0)
            GlStateManager._bindTexture(blurFramebuffer.colorAttachment)
            glUniform1i(pointer, 0)
        })
    )

    fun dumpFrameBuffer(framebuffer: Framebuffer) {
        val framebufferWidth = framebuffer.textureWidth
        val framebufferHeight = framebuffer.textureHeight

        NativeImage(framebufferWidth, framebufferHeight, false).use { nativeImage ->
            RenderSystem.bindTexture(framebuffer.colorAttachment)
            nativeImage.loadFromTextureImage(0, false)
            nativeImage.mirrorVertically()

            val file = File("screenshots")
            file.mkdir()

            val filename = "framebuffer_dump_${framebuffer.hashCode()}.png"
            nativeImage.writeTo(File(file, filename))
        }
    }

    fun renderQuad() {
        val builder = Tessellator.getInstance()
        val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        buffer.vertex(-1F, -1F, 0F).texture(0F, 0F)
        buffer.vertex(1F, -1F, 0F).texture(1F, 0F)
        buffer.vertex(1F, 1F, 0F).texture(1F, 1F)
        buffer.vertex(-1F, 1F, 0F).texture(0F, 1F)
        BufferRenderer.draw(buffer.end())
    }

    fun renderGaussianBlurFullResolution(source: Framebuffer = PostProcessRenderer.sourceFramebuffer) {
        StateIsolation.isolate(FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(), BlendState(false)) {
            glBindTexture(GlConst.GL_TEXTURE_2D, PostProcessRenderer.sourceFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, blurFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            blurFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
            GaussianBlurRenderer.fullPing.beginWrite(true)
            GaussianBlurRenderer.direction = Vector2f(1F, 0F)
            GaussianBlurRenderer.colorAttachment = source.colorAttachment
            GaussianBlurRenderer.gaussianBlurShader.blit()

            GaussianBlurRenderer.fullPong.beginWrite(true)
            GaussianBlurRenderer.direction = Vector2f(0F, 1F)
            GaussianBlurRenderer.colorAttachment = GaussianBlurRenderer.fullPing.colorAttachment
            GaussianBlurRenderer.gaussianBlurShader.blit()

            GaussianBlurRenderer.fullPong nearestCopyTo blurFramebuffer
        }
    }

    fun renderGaussianBlur(source: Framebuffer = PostProcessRenderer.sourceFramebuffer, target: Framebuffer = blurFramebuffer) {
        StateIsolation.isolate(FramebufferState.captureSnapshot(), ViewportState.captureSnapshot(), BlendState(false)) {
            glBindTexture(GlConst.GL_TEXTURE_2D, PostProcessRenderer.sourceFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, ScaleSampling.getDownScalingFramebuffer(1.0 / 2).colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, ScaleSampling.getDownScalingFramebuffer(1.0 / 4).colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, blurFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            source linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 2)
            ScaleSampling.getDownScalingFramebuffer(1.0 / 2) linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 4)
            val downscalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(1.0 / 4)

            // blurFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
            GaussianBlurRenderer.ping.beginWrite(true)
            GaussianBlurRenderer.direction = Vector2f(1F, 0F)
            GaussianBlurRenderer.colorAttachment = downscalingFramebuffer.colorAttachment
            GaussianBlurRenderer.gaussianBlurShader.blit()

            GaussianBlurRenderer.pong.beginWrite(true)
            GaussianBlurRenderer.direction = Vector2f(0F, 1F)
            GaussianBlurRenderer.colorAttachment = GaussianBlurRenderer.ping.colorAttachment
            GaussianBlurRenderer.gaussianBlurShader.blit()

            GaussianBlurRenderer.pong linearCopyTo halfResolutionBlurFramebuffer
            halfResolutionBlurFramebuffer linearCopyTo target
        }
    }

    fun renderKawaseBlur() {
        StateIsolation.isolate(FramebufferState.captureSnapshot(), ViewportState.captureSnapshot()) {
            glBindTexture(GlConst.GL_TEXTURE_2D, PostProcessRenderer.sourceFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, ScaleSampling.getDownScalingFramebuffer(1.0 / 2).colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, ScaleSampling.getDownScalingFramebuffer(1.0 / 4).colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            glBindTexture(GlConst.GL_TEXTURE_2D, blurFramebuffer.colorAttachment)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)

            PostProcessRenderer.sourceFramebuffer linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 2)
            ScaleSampling.getDownScalingFramebuffer(1.0 / 2) linearCopyTo ScaleSampling.getDownScalingFramebuffer(1.0 / 4)
            val downscalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(1.0 / 4)

            blurFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)

            kawaseOffset = 0F
            val shaders = mutableListOf<BlitProgram>()
            for (i in 0..4) {
                shaders.add(kawaseBlurShader)
            }
            PostProcessRenderer.useFramebuffer(downscalingFramebuffer) {
                PostProcessRenderer.renderShadersToFramebuffer(shaders, blurFramebuffer)
            }
        }
    }

    fun renderBlur() {
        renderGaussianBlur()
        // renderKawaseBlur()
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        blurFramebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
        // GL30.glViewport(0, 0, width, height)
    }
}