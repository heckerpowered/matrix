package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL20.glUniform1i
import org.lwjgl.opengl.GL46

object SignedDistanceField {
    var framebufferObject: Int = -1
    var originFramebufferObject: Int = -1
    var stepSize: Float = 1.0F

    init {
        RenderSystem.assertOnRenderThread()
    }

    val opacityMaskShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/opacity_mask.fsh"),
        arrayOf(
            UniformProvider("colorAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, framebufferObject)
                glUniform1i(pointer, 1)
            },
            UniformProvider("opacityMask") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, framebufferObject)
                glUniform1i(pointer, 0)
            }
        )
    )

    val seedGenShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/sdf/seed_gen.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            }
        )
    )

    val jumpFloodingShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/sdf/jump_flooding.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
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
                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            },
            UniformProvider("originFramebuffer") { pointer ->
                glActiveTexture(GlConst.GL_TEXTURE1)
                glBindTexture(GlConst.GL_TEXTURE_2D, originFramebufferObject)
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