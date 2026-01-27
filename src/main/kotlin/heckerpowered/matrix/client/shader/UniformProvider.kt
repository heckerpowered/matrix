/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.client.render.MatrixRenderSystem
import org.joml.Matrix4f
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil
import org.slf4j.MarkerFactory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private val buffer = MemoryUtil.memAllocFloat(16)

val projectionMatrixProvider = UniformProvider("projectionMatrix") { pointer ->
    buffer.position(0)
    RenderSystem.getProjectionMatrix().get(buffer)
    glUniformMatrix4fv(pointer, false, buffer)
}

val modelViewMatrixProvider = UniformProvider("modelViewMatrix") { pointer ->
    buffer.position(0)
    RenderSystem.getModelViewMatrix().get(buffer)
    glUniformMatrix4fv(pointer, false, buffer)
}

val inverseProjectionMatrixProvider = UniformProvider("inverseProjectionMatrix") { pointer ->
    buffer.position(0)

    val projectionMatrix = projectionMatrix
    val invertProjectionMatrix = Matrix4f(projectionMatrix).invert()
    invertProjectionMatrix.get(buffer)

    glUniformMatrix4fv(pointer, false, buffer)
}

val inverseModelViewMatrixProvider = UniformProvider("inverseModelViewMatrix") { pointer ->
    buffer.position(0)

    val modelViewMatrix = RenderSystem.getModelViewMatrix()
    val invertModelViewMatrix = Matrix4f(modelViewMatrix).invert()
    invertModelViewMatrix.get(buffer)

    glUniformMatrix4fv(pointer, false, buffer)
}

val inverseViewMatrixProvider = UniformProvider("inverseViewMatrix") { pointer ->
    buffer.position(0)
    MatrixRenderSystem.inverseViewMatrix.get(buffer)
    glUniformMatrix4fv(pointer, false, buffer)
}

val viewMatrixProvider = UniformProvider("inverseViewMatrix") { pointer ->
    buffer.position(0)
    MatrixRenderSystem.viewMatrix.get(buffer)
    glUniformMatrix4fv(pointer, false, buffer)
}

val viewProjectionMatrixProvider = UniformProvider("viewProjectionMatrix") { pointer ->
    buffer.position(0)
    MatrixRenderSystem.viewProjectionMatrix.get(buffer)
    glUniformMatrix4fv(pointer, false, buffer)
}

val playerPositionProvider = UniformProvider("playerPosition") { pointer ->
    val tickDelta = minecraft.renderTickCounter.getTickDelta(true)
    val position = player.getLerpedPos(tickDelta)
    glUniform3f(pointer, position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
}

val cameraPositionProvider = UniformProvider("cameraPosition") { pointer ->
    val camera = minecraft.gameRenderer.camera
    glUniform3f(pointer, camera.pos.x.toFloat(), camera.pos.y.toFloat(), camera.pos.z.toFloat())
}

val resolutionProvider = UniformProvider("resolution") { pointer ->
    val width = minecraft.window.framebufferWidth.toFloat()
    val height = minecraft.window.framebufferHeight.toFloat()
    glUniform2f(pointer, width, height)
}

val timeProvider = UniformProvider("time") { pointer ->
    glUniform1f(pointer, System.nanoTime().milliseconds.toDouble(DurationUnit.SECONDS).toFloat())
}

open class UniformProvider(val name: String, val set: (pointer: Int) -> Unit) {
    companion object {
        private val MARKER = MarkerFactory.getMarker("UniformProvider")
    }

    var pointer = -1

    fun init(program: Int) {
        pointer = glGetUniformLocation(program, name)
        if (pointer == -1) {
            Matrix.LOGGER.error(MARKER, "Cannot find uniform location, name: $name")
        }
    }
}