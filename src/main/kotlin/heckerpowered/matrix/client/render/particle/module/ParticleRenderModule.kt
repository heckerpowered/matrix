/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.capabilities.RasterizerDiscardState
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.viewMatrix
import org.lwjgl.opengl.GL46
import org.lwjgl.system.MemoryUtil

abstract class ParticleRenderModule : ParticleModule() {
    companion object {
        private val BUFFER = MemoryUtil.memAllocFloat(16)

        val PROJECTION_MATRIX_PROVIDER = UniformProvider("ProjectionMatrix") { pointer ->
            BUFFER.position(0)
            projectionMatrix.get(BUFFER)
            GL46.glUniformMatrix4fv(pointer, false, BUFFER)
        }

        val MODEL_VIEW_MATRIX_PROVIDER = UniformProvider("ModelViewMatrix") { pointer ->
            BUFFER.position(0)
            viewMatrix.get(BUFFER)
            GL46.glUniformMatrix4fv(pointer, false, BUFFER)
        }
    }

    override fun disableRasterizer(stateIsolation: StateIsolation) {
    }

    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        stateIsolation.push(RasterizerDiscardState(false))
        particleStates.bind(stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        // particleStates.unbind()
    }
}