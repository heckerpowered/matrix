/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.OpenGLExtensions.getErrorName
import heckerpowered.matrix.core.math.Vector3fExtensions.unaryMinus
import net.minecraft.client.render.Camera
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.lwjgl.opengl.GL46.*

object MatrixRenderSystem {
    var projectionMatrix = Matrix4f()
    var viewMatrix = Matrix4f()
    var viewProjectionMatrix = Matrix4f()
    var inverseProjectionMatrix = Matrix4f()
    var inverseViewMatrix = Matrix4f()
    var inverseViewProjectionMatrix = Matrix4f()

    @JvmStatic
    fun setupMatrix(camera: Camera, projectionMatrix: Matrix4fc) {
        this.projectionMatrix = Matrix4f(projectionMatrix)

        val rotation = camera.rotation.conjugate(Quaternionf())
        val translation = -camera.pos.toVector3f()

        viewMatrix.identity()
            .rotate(rotation)
            .translate(translation)

        viewProjectionMatrix.identity().mul(projectionMatrix).mul(viewMatrix)
        projectionMatrix.invert(inverseProjectionMatrix)
        inverseViewMatrix.identity()
            .translate(camera.pos.toVector3f())
            .rotate(camera.rotation)
        viewProjectionMatrix.invert(inverseViewProjectionMatrix)
    }

    fun assertOnRenderThread() {
        RenderSystem.assertOnRenderThread()
    }

    fun createShader(type: Int): Int {
        assertOnRenderThread()
        val shader = glCreateShader(type)
        if (shader != 0) {
            return shader
        }

        val error = glGetError()
        if (error == GL_INVALID_ENUM) {
            error("GL_INVALID_ENUM: shaderType $type is not an accepted value.")
        }
        val errorName = getErrorName(error)
        error("$errorName: shaderType: $type")
    }
}