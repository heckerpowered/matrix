/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.shader

import heckerpowered.matrix.Matrix
import org.lwjgl.opengl.GL31
import org.slf4j.MarkerFactory

open class UniformBufferProvider(val name: String, val set: (program: Int, pointer: Int) -> Unit) {
    companion object {
        private val MARKER = MarkerFactory.getMarker("UniformBufferProvider")
    }

    var pointer = GL31.GL_INVALID_INDEX

    fun init(program: Int) {
        pointer = GL31.glGetUniformBlockIndex(program, name)
        if (pointer == GL31.GL_INVALID_INDEX) {
            Matrix.LOGGER.error(MARKER, "Cannot find uniform block, name: $name")
        }
    }
}