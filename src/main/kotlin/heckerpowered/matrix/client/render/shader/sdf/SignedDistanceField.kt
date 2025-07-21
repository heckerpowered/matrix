package heckerpowered.matrix.client.render.shader.sdf

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL46

object SignedDistanceField {
    var framebufferObject: Int = -1
    var originFramebufferObject: Int = -1
    var stepSize: Float = 1.0F

    init {
        RenderSystem.assertOnRenderThread()
    }

    val seedGenShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/sdf/seed_gen.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                GL13.glActiveTexture(GlConst.GL_TEXTURE0)
                GL11.glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            }
        )
    )

    val jumpFloodingShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/sdf/jump_flooding.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                GL13.glActiveTexture(GlConst.GL_TEXTURE0)
                GL11.glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            },
            UniformProvider("stepSize") { pointer ->
                GL46.glUniform1f(pointer, stepSize)
            }
        )
    )

    val sdfEvalShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/sdf/sdf_eval.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                GL13.glActiveTexture(GlConst.GL_TEXTURE0)
                GL11.glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            },
            UniformProvider("originFramebuffer") { pointer ->
                GL13.glActiveTexture(GlConst.GL_TEXTURE1)
                GL11.glBindTexture(GlConst.GL_TEXTURE_2D, originFramebufferObject)
                RenderSystem.glUniform1i(pointer, 1)
            }
        )
    )

    fun generateStepSizes(width: Int, height: Int): List<Int> {
        val maxRes = maxOf(width, height)
        var step = 1
        while (step * 2 <= maxRes) {
            step *= 2
        }
        val steps = mutableListOf<Int>()
        while (step >= 1) {
            steps.add(step)
            step /= 2
        }
        return steps
    }

    fun computeSignedDistanceField(source: Framebuffer, pingFramebuffer: Framebuffer, pongFramebuffer: Framebuffer): Framebuffer {
        framebufferObject = source.colorAttachment
        originFramebufferObject = source.colorAttachment
        PostProcessRenderer.renderShaderToFramebuffer(seedGenShader, pingFramebuffer)
        this.framebufferObject = pingFramebuffer.colorAttachment

        val resolutionX = source.textureWidth
        val resolutionY = source.textureHeight

        var ping = pingFramebuffer
        var pong = pongFramebuffer
        for (stepSize in generateStepSizes(resolutionX, resolutionY)) {
            this.stepSize = stepSize.toFloat()
            val swap = ping
            ping = pong
            pong = swap

            framebufferObject = pong.colorAttachment
            PostProcessRenderer.renderShaderToFramebuffer(jumpFloodingShader, ping)
        }

        framebufferObject = ping.colorAttachment
        PostProcessRenderer.renderShaderToFramebuffer(sdfEvalShader, pong)
        return pong
    }

    fun computeSignedDistanceField(source: Framebuffer): Framebuffer {
        PostProcessRenderer.resetFramebuffers()
        return computeSignedDistanceField(source, PostProcessRenderer.ping, PostProcessRenderer.pong)
    }
}