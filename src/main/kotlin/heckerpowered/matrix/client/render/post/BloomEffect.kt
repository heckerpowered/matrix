package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.BlurRenderer
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL31

object BloomEffect {
    private val brightFramebuffer = PostProcessRenderer.createManagedFramebuffer()
    private val bloomFramebuffer = PostProcessRenderer.createManagedFramebuffer()
    private val brightnessShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/bloom/bloom_brightness_pass.fsh"),
            arrayOf(
                UniformProvider("framebuffer") { pointer ->
                    val framebuffer = minecraft.framebuffer

                    GL31.glActiveTexture(GlConst.GL_TEXTURE0)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                    GL31.glUniform1i(pointer, 0)
                },
                UniformProvider("threshold") { pointer ->
                    GL31.glUniform1f(pointer, 0.6F)
                }
            )
        )
    }

    fun renderBloom() {
        brightFramebuffer.clear(false)
        bloomFramebuffer.clear(false)
        ScaleSampling.getScaledFramebuffer(1.0).clear(false)
        ScaleSampling.getScaledFramebuffer(0.5).clear(false)

        minecraft.framebuffer.beginWrite(false)
        PostProcessRenderer.renderShaderToFramebuffer(brightnessShader, brightFramebuffer)
        PostProcessRenderer.copyFramebuffer(brightFramebuffer, ScaleSampling.getScaledFramebuffer(1.0))
        for (i in 2..5) {
            val previousScale = 1.0 / (i - 1.0)
            val currentScale = 1.0 / i

            val previousScalingFramebuffer = ScaleSampling.getScaledFramebuffer(previousScale)
            val currentScalingFramebuffer = ScaleSampling.getScaledFramebuffer(currentScale)

            ScaleSampling.levelOfDetail = i.toFloat()
            ScaleSampling.sample(previousScalingFramebuffer, currentScalingFramebuffer, ScaleSampling.textureLod)
        }

        for (i in (2..5).reversed()) {
            val previousScale = 1.0 / i
            val currentScale = 1.0 / (i - 1.0)

            val previousScalingFramebuffer = ScaleSampling.getScaledFramebuffer(previousScale)
            val currentScalingFramebuffer = ScaleSampling.getScaledFramebuffer(currentScale)

            ScaleSampling.levelOfDetail = i.toFloat()
            ScaleSampling.sample(previousScalingFramebuffer, currentScalingFramebuffer, ScaleSampling.textureLod)
        }
        BlurRenderer.dumpFrameBuffer(ScaleSampling.getScaledFramebuffer(1.0))
    }
}