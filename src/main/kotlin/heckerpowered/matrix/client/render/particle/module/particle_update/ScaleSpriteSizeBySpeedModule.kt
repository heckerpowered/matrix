/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL46

class ScaleSpriteSizeBySpeedModule : ParticleUpdateModule() {
    companion object {
        var minScaleFactor = 0.0F
        var maxScaleFactor = 1.0F
        var velocityThreshold = 1.0F

        private val Program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_update/scale_sprite_size_by_speed.vsh", GL46.GL_VERTEX_SHADER),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                UniformProvider("MinScaleFactor") { pointer -> glUniform1f(pointer, minScaleFactor) },
                UniformProvider("MaxScaleFactor") { pointer -> glUniform1f(pointer, maxScaleFactor) },
                UniformProvider("VelocityThreshold") { pointer -> glUniform1f(pointer, velocityThreshold) }
            )
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        Program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        Program.disableShader()
    }
}