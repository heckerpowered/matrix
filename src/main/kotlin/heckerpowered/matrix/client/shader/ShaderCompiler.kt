/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.client.render.OpenGLExtensions
import heckerpowered.matrix.client.shader.cache.ShaderCompilationException
import kotlinx.coroutines.*
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

object ShaderCompiler {
    private fun shaderSource(shader: Int, source: String) {
        try {
            MemoryStack.stackPush().use { memoryStack ->
                val sourceBuffer = memoryStack.UTF8(source, false)
                val pointerBuffer = memoryStack.mallocPointer(1)
                    .put(sourceBuffer)
                    .flip()
                val lengthBuffer = memoryStack.mallocInt(1)
                    .put(sourceBuffer.capacity())
                    .flip()
                glShaderSource(shader, pointerBuffer, lengthBuffer)
                return
            }
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val sourceBuffer = MemoryUtil.memUTF8(source, false)
        try {
            MemoryStack.stackPush().use { memoryStack ->
                val pointerBuffer = memoryStack.mallocPointer(1)
                    .put(sourceBuffer)
                    .flip()
                val lengthBuffer = memoryStack.mallocInt(1)
                    .put(sourceBuffer.capacity())
                    .flip()
                glShaderSource(shader, pointerBuffer, lengthBuffer)
            }
        } finally {
            MemoryUtil.memFree(sourceBuffer)
        }
    }

    fun compileShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
            val logLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH)
            val log = glGetShaderInfoLog(shader, logLength)
            throw ShaderCompilationException("$log\n$source")
        }

        return shader
    }

    @OptIn(DelicateCoroutinesApi::class)
    val Dispatcher = newFixedThreadPoolContext(1, "Shader-Compile")

    private fun ensureGLContext() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            OpenGLExtensions.initGLContext("Shader-Compile")
        }
    }

    fun CoroutineScope.compileShaderAsync(source: String, type: Int): Deferred<Int> = async(Dispatcher) {
        ensureGLContext()
        compileShader(source, type)
    }
}