/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle.memory

import heckerpowered.matrix.client.render.particle.module.ParticleStateElement
import heckerpowered.matrix.client.render.particle.module.ParticleStateElement.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

class DefaultLayout : MemoryLayout() {
    companion object {
        val sharedUniformBufferObject by lazy {
            val uniformBufferObject = glGenBuffers()

            glBindBuffer(GL_UNIFORM_BUFFER, uniformBufferObject)
            glBufferData(GL_UNIFORM_BUFFER, DEFAULT_LAYOUT.bufferSizeBytes.toLong(), GL_DYNAMIC_DRAW)
            glBindBuffer(GL_UNIFORM_BUFFER, 0)

            return@lazy uniformBufferObject
        }
    }

    override val bufferSizeBytes: Int
        get() = 24 * Float.SIZE_BYTES
    override val floats: Int
        get() = 24

    override fun getPosition(element: ParticleStateElement) = when (element) {
        POSITION_X -> 0
        POSITION_Y -> 1
        POSITION_Z -> 2

        VELOCITY_X -> 3
        VELOCITY_Y -> 4
        VELOCITY_Z -> 5

        ACCELERATION_X -> 6
        ACCELERATION_Y -> 7
        ACCELERATION_Z -> 8

        SPRITE_SIZE -> 9
        SCALE -> 10
        AGE -> 11
        LIFETIME -> 12

        COLOR_R -> 13
        COLOR_G -> 14
        COLOR_B -> 15
        COLOR_A -> 16

        ORIENTATION_X -> 17
        ORIENTATION_Y -> 18
        ORIENTATION_Z -> 19
        ORIENTATION_W -> 20

        ANGULAR_VELOCITY_X -> 21
        ANGULAR_VELOCITY_Y -> 22
        ANGULAR_VELOCITY_Z -> 23
    }

    override val sharedUniformBufferObject: Int
        get() = DefaultLayout.sharedUniformBufferObject
}