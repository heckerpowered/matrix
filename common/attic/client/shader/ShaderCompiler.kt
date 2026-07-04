/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.opengl.GlConst
import heckerpowered.matrix.client.render.MatrixRenderSystem
import heckerpowered.matrix.client.shader.cache.ShaderCompilationException
import org.lwjgl.opengl.ARBShadingLanguageInclude
import org.lwjgl.opengl.GL20.*

object ShaderCompiler {
    fun compileShader(descriptor: ShaderSourceDescriptor): Int {
        val shader = MatrixRenderSystem.createShader(descriptor.stage.shaderType)

        glShaderSource(shader, descriptor.source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
            val logLength = glGetShaderi(shader, GL_INFO_LOG_LENGTH)
            val log = glGetShaderInfoLog(shader, logLength)
            throw ShaderCompilationException("$log\n${descriptor.source}")
        }

        return shader
    }

    fun addInclude(virtualPath: String, shaderSource: String) {
        // println("Add include: $virtualPath")
        ARBShadingLanguageInclude.glNamedStringARB(ARBShadingLanguageInclude.GL_SHADER_INCLUDE_ARB, virtualPath, shaderSource)
    }
}