/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.state.BlendFuncSeparateState
import heckerpowered.matrix.client.render.state.FramebufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.ViewportState
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL46.*

object ToneMapping {
    var exposureLinear: Float = 1.0f
    var exposureEv: Float = 0.0f

    private val toneMappingProgram by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/tone_mapping/aces_filmic.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                UniformProvider("hdrScene") { location ->
                    glActiveTexture(GlConst.GL_TEXTURE0)
                    glBindTexture(GlConst.GL_TEXTURE_2D, currentSource.colorAttachment)
                    glUniform1i(location, 0)
                },
                UniformProvider("exposure") { location ->
                    glUniform1f(location, exposureLinear)
                },
                UniformProvider("exposureEv") { location ->
                    glUniform1f(location, exposureEv)
                }
            )
        )
    }

    private lateinit var currentSource: Framebuffer
    val toneMapFramebuffer = PostProcessRenderer.createManagedFramebuffer()

    fun render(sourceFramebuffer: Framebuffer, targetFramebuffer: Framebuffer) {
        currentSource = sourceFramebuffer
        StateIsolation.isolate(
            FramebufferState(targetFramebuffer),
            ViewportState(targetFramebuffer),
            BlendState.captureSnapshot(),
            BlendFuncSeparateState.captureSnapshot()
        ) {
            PostProcessRenderer.renderShaderToFramebuffer(toneMappingProgram, targetFramebuffer, false)
        }
    }
}