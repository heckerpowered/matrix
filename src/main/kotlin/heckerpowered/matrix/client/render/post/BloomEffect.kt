package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.*
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector4f
import org.lwjgl.opengl.GL31
import org.lwjgl.opengl.GL46

object BloomEffect {
    private val brightFramebuffer = PostProcessRenderer.createManagedFramebuffer()
    val bloomFramebuffer = PostProcessRenderer.createManagedFramebuffer()

    var brightnessPassFramebuffer: Framebuffer = minecraft.framebuffer
    var brightnessThreshold = 0F
    var bloomIntensity = 1.0F
    private val brightnessShader by lazy {
        BlitShader(
            resourceToString("/assets/matrix/shaders/sobel.vert"),
            resourceToString("/assets/matrix/shaders/post/bloom/bloom_brightness_pass.fsh"),
            arrayOf(
                UniformProvider("framebuffer") { pointer ->
                    val framebuffer = brightnessPassFramebuffer
                    GL31.glActiveTexture(GlConst.GL_TEXTURE0)
                    GL31.glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                    GL31.glUniform1i(pointer, 0)
                },
                UniformProvider("threshold") { pointer ->
                    GL31.glUniform1f(pointer, brightnessThreshold)
                },
                UniformProvider("intensity") { pointer ->
                    GL31.glUniform1f(pointer, bloomIntensity)
                }
            )
        )
    }

    fun renderBloom() {
        brightFramebuffer.clear(false)
        bloomFramebuffer.clear(false)

        PostProcessRenderer.renderShaderToFramebuffer(brightnessShader, brightFramebuffer)

        var lastFramebuffer = ScaleSampling.getDownScalingFramebuffer(1.0)
        brightFramebuffer copyTo lastFramebuffer
        val mipLevel = minecraft.framebuffer.recommendMipLevel()
        var resolutionScale = 1.0
        val resolutionScalePerLevel = 2
        for (i in 0..<mipLevel) {
            resolutionScale /= resolutionScalePerLevel
            val currentScalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(resolutionScale)
            val previousScalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(resolutionScale * resolutionScalePerLevel)

            GL46.glBindTexture(GlConst.GL_TEXTURE_2D, previousScalingFramebuffer.colorAttachment)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR)

            previousScalingFramebuffer tent currentScalingFramebuffer
            lastFramebuffer = currentScalingFramebuffer
        }

        lastFramebuffer copyTo ScaleSampling.getUpScalingFramebuffer(resolutionScale)
        bloomIntensity = 1F
        colorMultiplier = Vector4f(bloomIntensity, bloomIntensity, bloomIntensity, bloomIntensity)
        for (i in (0..<mipLevel).reversed()) {
            resolutionScale *= resolutionScalePerLevel

            val currentScalingFramebuffer = ScaleSampling.getUpScalingFramebuffer(resolutionScale)
            val previousScalingFramebuffer = ScaleSampling.getUpScalingFramebuffer(resolutionScale / resolutionScalePerLevel)

            val previousDownScalingFramebuffer = ScaleSampling.getDownScalingFramebuffer(resolutionScale / resolutionScalePerLevel)

            GL46.glBindTexture(GlConst.GL_TEXTURE_2D, previousScalingFramebuffer.colorAttachment)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR)

            GL46.glBindTexture(GlConst.GL_TEXTURE_2D, previousDownScalingFramebuffer.colorAttachment)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR)
            GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR)

            previousScalingFramebuffer copyTo currentScalingFramebuffer
            previousDownScalingFramebuffer tent previousDownScalingFramebuffer
            currentScalingFramebuffer.draw {
                previousDownScalingFramebuffer blend currentScalingFramebuffer
            }
        }

        colorMultiplier = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
        RenderSystem.enableBlend()
        PostProcessRenderer.copyFramebuffer(ScaleSampling.getUpScalingFramebuffer(1.0), bloomFramebuffer, false)
    }
}