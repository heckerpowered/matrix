/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.shader.component.ShaderComponent

open class BlitProgram(
    vararg shaders: Shader,
    uniforms: Array<UniformProvider> = emptyArray(),
    uniformBuffers: Array<UniformBufferProvider> = emptyArray(),
    components: Array<ShaderComponent> = emptyArray(),
) :
    Program(*shaders, uniforms = uniforms, uniformBuffers = uniformBuffers, components = components) {
    companion object {
        fun blit() {
        }
    }

    fun blit() {
    }
}
