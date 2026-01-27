/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.lwjgl.opengl.*

/**
 * Enumerates all GLSL stages with both "shader type enum" and "stage bit".
 *
 * @author heckerpowered
 */
enum class ShaderStage(
    val shaderType: Int,
    val stageBit: Int,
) {
    VERTEX(GL20.GL_VERTEX_SHADER, GL41.GL_VERTEX_SHADER_BIT),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER, GL41.GL_FRAGMENT_SHADER_BIT),
    GEOMETRY(GL32.GL_GEOMETRY_SHADER, GL41.GL_GEOMETRY_SHADER_BIT),
    TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER, GL41.GL_TESS_CONTROL_SHADER_BIT),
    TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER, GL41.GL_TESS_EVALUATION_SHADER_BIT),
    COMPUTE(GL43.GL_COMPUTE_SHADER, GL43.GL_COMPUTE_SHADER_BIT);

    companion object {
        fun detectByPath(path: String): ShaderStage? {
            if (path.endsWith(".vsh")) {
                return VERTEX
            }
            if (path.endsWith(".fsh")) {
                return FRAGMENT
            }
            if (path.endsWith(".gsh")) {
                return GEOMETRY
            }
            if (path.endsWith(".tcsh")) {
                return TESS_CONTROL
            }
            if (path.endsWith(".tesh")) {
                return TESS_EVALUATION
            }
            if (path.endsWith(".csh")) {
                return COMPUTE
            }

            return null
        }
    }
}