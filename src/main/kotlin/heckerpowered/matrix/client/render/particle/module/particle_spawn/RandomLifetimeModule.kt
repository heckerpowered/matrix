/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL46

class RandomLifetimeModule : ParticleSpawnModule() {
    companion object {
        var minLifetime = 0.8F
        var maxLifetime = 1.75F

        private val program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_spawn/random_lifetime.vsh", GL46.GL_VERTEX_SHADER),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                UniformProvider("MinLifetime") { pointer -> glUniform1f(pointer, minLifetime) },
                UniformProvider("MaxLifetime") { pointer -> glUniform1f(pointer, maxLifetime) }
            ),
        )
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        program.disableShader()
    }
}
