/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import net.minecraft.client.Camera
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf

object MatrixRenderSystem {
    var projectionMatrix = Matrix4f()
    var viewMatrix = Matrix4f()
    var viewProjectionMatrix = Matrix4f()
    var inverseViewMatrix = Matrix4f()
    var inverseViewProjectionMatrix = Matrix4f()

    @JvmStatic
    fun setupMatrix(camera: Any?, projectionMatrix: Matrix4fc) {
        this.projectionMatrix.set(projectionMatrix)
        viewMatrix.setView(camera)
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix)
        inverseViewMatrix.set(viewMatrix).invert()
        inverseViewProjectionMatrix.set(viewProjectionMatrix).invert()
    }

    private fun Matrix4f.setView(camera: Any?) {
        identity()
        if (camera !is Camera) {
            return
        }

        rotate(camera.rotation().conjugate(Quaternionf()))
        val position = camera.position()
        translate(-position.x.toFloat(), -position.y.toFloat(), -position.z.toFloat())
    }

    fun createShader(shaderType: Int): Int = 0
}
