/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.shader.component.ShaderComponent
import java.io.Closeable

open class Program(
    vararg shaders: Shader,
    private val uniforms: Array<UniformProvider> = emptyArray(),
    private val uniformBuffers: Array<UniformBufferProvider> = emptyArray(),
    val components: Array<ShaderComponent> = emptyArray(),
) : Closeable {
    companion object {
        internal var activeProgram: Program? = null
    }

    protected val shaderStages: List<Shader> = shaders.toList()

    @Volatile
    var program = 0

    protected fun attachShader(program: Int, shader: Int?) {
    }

    protected fun deleteShader(shader: Int?) {
    }

    override fun close() {
        if (activeProgram == this) {
            activeProgram = null
        }
    }

    fun enableShader() {
        activeProgram = this
    }

    fun uploadUniforms() {
    }

    fun initUniforms() {
    }

    fun disableShader() {
        if (activeProgram == this) {
            activeProgram = null
        }
    }
}
