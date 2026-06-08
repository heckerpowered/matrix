/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.component.ShaderComponent
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER

open class BlitProgram(
    vararg shaders: Shader,
    uniforms: Array<UniformProvider> = emptyArray(),
    uniformBuffers: Array<UniformBufferProvider> = emptyArray(),
    components: Array<ShaderComponent> = emptyArray(),
) :
    Program(*shaders, uniforms = uniforms, uniformBuffers = uniformBuffers, components = components) {
    companion object {
        fun blit() {
            (activeProgram as? BlitProgram)?.blit()
        }
    }

    fun blit() {
        PostProcessRenderer.renderShaderToFramebuffer(this, PostProcessRenderer.currentFramebuffer())
        PostProcessRenderer.nextFramebuffer()
    }

    fun fragmentResourcePath(): String? {
        return shaderStages
            .filterIsInstance<ResourceShader>()
            .firstOrNull { it.type == GL_FRAGMENT_SHADER }
            ?.path
    }
}
