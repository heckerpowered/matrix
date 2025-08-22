/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle.memory

import heckerpowered.matrix.client.render.particle.module.ParticleStateElement

abstract class MemoryLayout {
    companion object {
        val STD_140 = Std140Layout()
        val DEFAULT_LAYOUT = DefaultLayout()
    }

    val name: String
        get() = this::class.simpleName ?: "UnknownLayout"

    abstract val bufferSizeBytes: Int

    /**
     * The number of floats in the layout, including padding.
     */
    abstract val floats: Int
    abstract fun getPosition(element: ParticleStateElement): Int
    abstract val sharedUniformBufferObject: Int

    operator fun get(element: ParticleStateElement): Int {
        return getPosition(element)
    }
}