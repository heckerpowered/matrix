/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.client.render.OpenGLExtensions
import heckerpowered.matrix.client.shader.component.ShaderComponent
import kotlinx.coroutines.*
import net.minecraft.client.gl.GlProgramManager
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46.*
import org.slf4j.MarkerFactory
import java.io.Closeable

open class Program(
    vararg shaders: Shader,
    private val uniforms: Array<UniformProvider> = emptyArray(),
    private val uniformBuffers: Array<UniformBufferProvider> = emptyArray(),
    val components: Array<ShaderComponent> = emptyArray(),
) : Closeable {
    companion object {
        private val MARKER = MarkerFactory.getMarker("UniformProvider")

        private fun ensureGLContext() {
            if (GLFW.glfwGetCurrentContext() == 0L) {
                OpenGLExtensions.initGLContext("Shader-Compile")
            }
        }

        private fun compileShader(source: String, type: Int): Int {
            ensureGLContext()
            return ShaderCompiler.compileShader(source, type)
        }
    }

    @Volatile
    var program = 0

    init {
        val creationStack = Throwable().stackTraceToString()
        CoroutineScope(ShaderCompiler.Dispatcher).launch {
            try {
                val shaderSourceDeferreds = shaders.map { async(Dispatchers.IO) { it.source; it } }
                val shaderSources = shaderSourceDeferreds.awaitAll()

                val shaderObjects = shaderSources.map { compileShader(it.source, it.type) }

                program = glCreateProgram()

                shaderObjects.forEach { glAttachShader(program, it) }
                components.forEach { component -> component.init(program) }

                glLinkProgram(program)
                if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
                    val log = glGetProgramInfoLog(program, 1024)
                    error("Failed to compile shader: $log")
                }

                shaderObjects.forEach { glDeleteShader(it) }
                initUniforms()
            } catch (exception: Exception) {
                exception.printStackTrace()
                println(creationStack)
            }
        }
    }

    protected fun attachShader(program: Int, shader: Int?) {
        if (shader != null) {
            glAttachShader(program, shader)
        }
    }

    protected fun deleteShader(shader: Int?) {
        if (shader != null) {
            glDeleteShader(shader)
        }
    }

    override fun close() {
        glDeleteProgram(program)
        components.forEach { component -> component.delete() }
    }

    fun enableShader() {
        if (program == 0) {
            return
        }
        GlProgramManager.useProgram(program)
        initUniforms()
        uploadUniforms()
        components.filter { it.enabled }.forEach { it.enable() }
    }

    fun uploadUniforms() {
        uniforms.forEach { uniform -> uniform.set(uniform.pointer) }
        uniformBuffers.forEach { uniformBuffer -> uniformBuffer.set(program, uniformBuffer.pointer) }
    }

    fun initUniforms() {
        uniforms.forEach { uniform -> uniform.init(program) }
        uniformBuffers.forEach { uniformBuffer -> uniformBuffer.init(program) }
    }

    fun disableShader() {
        components.filter { it.enabled }.forEach { it.disable() }
        GlProgramManager.useProgram(0)
    }
}