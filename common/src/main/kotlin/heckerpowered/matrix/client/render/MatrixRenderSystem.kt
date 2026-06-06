/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import org.joml.Matrix4f
import org.joml.Matrix4fc

object MatrixRenderSystem {
    var projectionMatrix = Matrix4f()
    var viewMatrix = Matrix4f()
    var viewProjectionMatrix = Matrix4f()
    var inverseViewMatrix = Matrix4f()
    var inverseViewProjectionMatrix = Matrix4f()

    @JvmStatic
    fun setupMatrix(camera: Any?, projectionMatrix: Matrix4fc) {
        this.projectionMatrix.set(projectionMatrix)
        viewMatrix.identity()
        viewProjectionMatrix.set(projectionMatrix)
        inverseViewMatrix.identity()
        viewProjectionMatrix.invert(inverseViewProjectionMatrix)
    }

    fun createShader(shaderType: Int): Int = 0
}
