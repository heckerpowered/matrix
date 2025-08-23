/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL46

class DragModule : ParticleUpdateModule() {
    companion object {
        var minDrag = 0.8F
        var maxDrag = 1.2F

        private val Program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_update/drag.vsh", GL46.GL_VERTEX_SHADER),
            components = arrayOf(TRANSFORM_FEEDBACK),
            uniforms = arrayOf(
                DELTA_TIME_PROVIDER,
                UniformProvider("MinDrag") { pointer -> glUniform1f(pointer, minDrag) },
                UniformProvider("MaxDrag") { pointer -> glUniform1f(pointer, maxDrag) }
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