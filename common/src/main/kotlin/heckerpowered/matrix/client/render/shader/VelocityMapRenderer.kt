/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.times
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.opengl.GL46

object VelocityMapRenderer {
    private var previousModelViewProjectionMatrix = Matrix4f()
    private var currentModelViewProjectionMatrix = Matrix4f()

    private val matrixBuffer = FloatArray(16)

    val velocityMap = PostProcessRenderer.createManagedFramebuffer()

    val velocityMapShader by lazy {
        BlitProgram(
            ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.vsh", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
                UniformProvider("previousModelViewProjectionMatrix") { pointer ->
                    previousModelViewProjectionMatrix.get(matrixBuffer)
                    GL46.glUniformMatrix4fv(pointer, false, matrixBuffer)
                },
                UniformProvider("currentModelViewProjectionMatrix") { pointer ->
                    currentModelViewProjectionMatrix.get(matrixBuffer)
                    GL46.glUniformMatrix4fv(pointer, false, matrixBuffer)
                }
            )
        )
    }

    init {
        // PostProcessCallback.event.register(::onPostProcess)
    }

    private fun onPostProcess() {
        previousModelViewProjectionMatrix = currentModelViewProjectionMatrix

        val modelViewMatrix = RenderSystem.getModelViewMatrix()
        val projectionMatrix = RenderSystem.getProjectionMatrix()

        // MVP = P * M * V
        currentModelViewProjectionMatrix = projectionMatrix * modelViewMatrix
    }
}