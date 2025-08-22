/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.core.resourceToString

/**
 * A [`Shader`](Shader) implementation that reads its GLSL source from a
 * classpath resource at the specified `path`.
 *
 * The shader text is loaded **lazily**—the file is read only when
 * [`source`][source] is accessed for the first time, and the result is cached thereafter.
 *
 * @property path
 *   The absolute classpath location of the shader file (e.g. `"/shaders/basic.vert.glsl"`).
 *
 * @constructor
 *   Creates a `ResourceShader` of the given OpenGL shader `type` that
 *   will load its source from the specified `path`.
 *
 * @param type
 *   The OpenGL shader type constant (such as `GL20.GL_VERTEX_SHADER` or `GL20.GL_FRAGMENT_SHADER`).
 */
class ResourceShader(
    private val path: String,
    type: Int,
) : Shader(type) {
    override val source: String by lazy { resourceToString(path) }
}