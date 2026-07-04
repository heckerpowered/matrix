/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.viewMatrix
import org.joml.Matrix4f

object VelocityMapRenderer {
    private var previousModelViewProjectionMatrix = Matrix4f()
    private var currentModelViewProjectionMatrix = Matrix4f()

    val velocityMap = PostProcessRenderer.createManagedFramebuffer()

    // TODO(26.2): velocity_map.vsh is a real per-vertex shader (motion vectors from mesh
    // geometry), incompatible with BlitProgram's fixed fullscreen-screenquad vertex stage.
    // This effect has no port path under the current wrapper API without a custom
    // RenderPipeline built outside BlitProgram. Feature disabled pending a dedicated
    // velocity-buffer pipeline.
    //
    // val velocityMapShader by lazy {
    //     BlitProgram(
    //         ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.vsh", GL_VERTEX_SHADER),
    //         ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.fsh", GL_FRAGMENT_SHADER),
    //         uniforms = arrayOf(
    //             UniformProvider("previousModelViewProjectionMatrix") { pointer ->
    //                 previousModelViewProjectionMatrix.get(matrixBuffer)
    //                 GL46.glUniformMatrix4fv(pointer, false, matrixBuffer)
    //             },
    //             UniformProvider("currentModelViewProjectionMatrix") { pointer ->
    //                 currentModelViewProjectionMatrix.get(matrixBuffer)
    //                 GL46.glUniformMatrix4fv(pointer, false, matrixBuffer)
    //             }
    //         )
    //     )
    // }

    init {
        // PostProcessCallback.event.register(::onPostProcess)
    }

    private fun onPostProcess() {
        previousModelViewProjectionMatrix = currentModelViewProjectionMatrix

        // MVP = P * V; RenderSystem.getModelViewMatrix()/getProjectionMatrix() no longer exist
        // on the 26.2 wrapper RenderSystem. Matches other converted call sites (e.g.
        // UniformProvider.kt's putViewProjectionMatrix), which source the current frame's
        // matrices from the top-level projectionMatrix/viewMatrix accessors
        // (backed by MatrixRenderSystem, populated once per frame via setupMatrix).
        currentModelViewProjectionMatrix = Matrix4f(projectionMatrix).mul(viewMatrix)
    }
}
