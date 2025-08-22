/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render

import org.joml.Matrix4f

object RenderSystem {
    fun createNdcToScreenMatrix(resolutionX: Float, resolutionY: Float): Matrix4f {
        return Matrix4f().set(
            resolutionX / 2f, 0f, 0f, resolutionX / 2f,
            0f, resolutionY / 2f, 0f, resolutionY / 2f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }
}