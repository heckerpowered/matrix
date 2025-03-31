package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gl.SimpleFramebuffer
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.NativeImage
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.io.File


object BlurRenderer {
    var initialFramebuffer = minecraft.framebuffer
    var currentFramebuffer = minecraft.framebuffer

    var radius = 5.0F
    var kawaseOffset = 1.0F

    private val imageProvider = UniformProvider("image") { pointer ->
        GlStateManager._activeTexture(GL13.GL_TEXTURE0)
        GlStateManager._bindTexture(currentFramebuffer.colorAttachment)
        GL20.glUniform1i(pointer, 0)
    }

    private val radiusProvider = UniformProvider("radius") { pointer ->
        GL20.glUniform1f(pointer, radius * MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat())
    }

    private val kawaseOffsetProvider = UniformProvider("offset") { pointer ->
        kawaseOffset += 3F
        val progress = MatrixHud.magicShownOpacityAnimation.animatedValue.toFloat()
        GL20.glUniform2f(pointer, progress * kawaseOffset, progress * kawaseOffset)
    }

    val blurFramebuffer by lazy {
        val framebuffer = SimpleFramebuffer(
            minecraft.window.framebufferWidth,
            minecraft.window.framebufferHeight,
            true,
            MinecraftClient.IS_SYSTEM_MAC
        )
        framebuffer.setClearColor(.0F, .0F, .0F, .0F)
        framebuffer
    }

    val horizontalBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/gaussian_blur_horizontal.fsh"),
        arrayOf(PostProcessRenderer.framebufferProvider, radiusProvider)
    )

    val verticalBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/gaussian_blur_vertical.fsh"),
        arrayOf(PostProcessRenderer.framebufferProvider, radiusProvider)
    )

    val kawaseBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blur/kawase_blur.fsh"),
        arrayOf(PostProcessRenderer.framebufferProvider, kawaseOffsetProvider)
    )

    val tentBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blur/tent.fsh"),
        arrayOf(PostProcessRenderer.framebufferProvider)
    )

    private val colorfulShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/color/colorful.fsh"),
        arrayOf(PostProcessRenderer.framebufferProvider,
            UniformProvider("brightness") { pointer ->
                GL20.glUniform1f(pointer, 1.1F)
            },
            UniformProvider("saturation") { pointer ->
                GL20.glUniform1f(pointer, 2.0F)
            },
            UniformProvider("contrast") { pointer ->
                GL20.glUniform1f(pointer, 1.0F)
            })
    )

    val blurTextureRenderShader = Shader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/blur_mask.fsh"),
        arrayOf(UniformProvider("image") { pointer ->
            GlStateManager._activeTexture(GL13.GL_TEXTURE0)
            GlStateManager._bindTexture(blurFramebuffer.colorAttachment)
            GL20.glUniform1i(pointer, 0)
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

    fun renderBlur() {
        val lowersamplingFramebuffer = ScaleSampling.getScaledFramebuffer(1.0 / 4)
        ScaleSampling.sample(PostProcessRenderer.sourceFramebuffer, lowersamplingFramebuffer, ScaleSampling.bilinearSample)

        blurFramebuffer.clear(MinecraftClient.IS_SYSTEM_MAC)
        minecraft.framebuffer.beginWrite(false)
        kawaseOffset = 0F
        val shaders = mutableListOf<BlitShader>()
        for (i in 0..4) {
            shaders.add(kawaseBlurShader)
        }

        PostProcessRenderer.useFramebuffer(lowersamplingFramebuffer) {
            PostProcessRenderer.renderShadersToFramebuffer(shaders, blurFramebuffer)
        }
    }

    @JvmStatic
    fun onResize(width: Int, height: Int) {
        blurFramebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC)
        // GL30.glViewport(0, 0, width, height)
    }
}