/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader.hud

import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object ProgressRingRenderer {
    /**
     * The progress of the progress ring, between 0.0F and 1.0F, inclusive.
     */
    var progress: Float = 1.0F

    /**
     * The radius of the progress ring in normalized device coordinates (NDC).
     *
     * 0.5 radius = full screen
     */
    var radius: Float = 0.5F

    /**
     * The thickness of the progress ring in normalized device coordinates (NDC).
     * From the edge of the ring to the center of the ring.
     */
    var thickness: Float = 0.1F

    /**
     * The center of the progress ring in normalized device coordinates (NDC).
     */
    var center: Vector2f = Vector2f(0.5F, 0.5F)

    /**
     * The color of the progress ring.
     */
    var color: Vector4f = Vector4f(1.0F)

    val progressRingShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/position_texture.fsh", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/hud/progress_ring.fsh", GL_FRAGMENT_SHADER),
    )
}
