/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object VelocityMapRenderer {
    val velocityMap = PostProcessRenderer.createManagedFramebuffer()
    val velocityMapShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.vsh", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/velocity_map/velocity_map.fsh", GL_FRAGMENT_SHADER),
    )
}
