/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.core.math.unaryMinus
import net.minecraft.client.Camera
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf

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

        val rotation = camera.rotation().conjugate(Quaternionf())
        val translation = -camera.position().toVector3f()

        viewMatrix.identity()
            .rotate(rotation)
            .translate(translation)

        viewProjectionMatrix.identity().mul(projectionMatrix).mul(viewMatrix)
        projectionMatrix.invert(inverseProjectionMatrix)
        inverseViewMatrix.identity()
            .translate(camera.position().toVector3f())
            .rotate(camera.rotation())
        viewProjectionMatrix.invert(inverseViewProjectionMatrix)
    }

    fun assertOnRenderThread() {
        RenderSystem.assertOnRenderThread()
    }
}