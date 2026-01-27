/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.Color
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER
import org.lwjgl.system.MemoryUtil

object PositionColorProgram : Program(
    ResourceShader("/assets/matrix/shaders/position_color.vsh", GL_VERTEX_SHADER),
    ResourceShader("/assets/matrix/shaders/position_color.fsh", GL_FRAGMENT_SHADER),
    uniforms = arrayOf(
        modelViewMatrixProvider,
        projectionMatrixProvider,
        UniformProvider("colorModulator") { pointer ->
            RenderSystem.glUniform3(pointer, PositionColorProgram.colorBuffer)
        }
    )
) {
    private val colorBuffer = MemoryUtil.memAllocFloat(4)
    var color = Color(1, 1, 1, 1)
        set(value) {
            field = value
            uploadBuffer()
        }

    private fun uploadBuffer() {
        val color = color
        val colorBuffer = colorBuffer
        colorBuffer.clear()
        colorBuffer.put(color.red / 255F)
        colorBuffer.put(color.green / 255F)
        colorBuffer.put(color.blue / 255F)
        colorBuffer.put(color.alpha / 255F)
    }
}