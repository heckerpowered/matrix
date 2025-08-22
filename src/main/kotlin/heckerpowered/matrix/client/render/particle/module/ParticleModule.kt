/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle.module

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.TransformFeedbackBindingState
import heckerpowered.matrix.client.render.state.capabilities.RasterizerDiscardState
import heckerpowered.matrix.client.shader.component.TransformFeedback
import org.lwjgl.opengl.GL11.GL_POINTS
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.*

abstract class ParticleModule {
    companion object {
        val TRANSFORM_FEEDBACK = TransformFeedback(
            varyingNames = arrayOf(
                "OutPosition", "OutVelocity", "OutAcceleration",
                "OutSpriteSize", "OutScale", "OutAge", "OutLifetime",
                "OutColor", "OutOrientation", "OutAngularVelocity",
            ),
            initOnly = true
        )

        val QUERY_OBJECT = glGenQueries()
    }

    protected open fun disableRasterizer(stateIsolation: StateIsolation) {
        stateIsolation.push(RasterizerDiscardState(true))
    }

    open fun bind(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount, stateIsolation: StateIsolation) {
        particleStates.bind(stateIsolation)
        disableRasterizer(stateIsolation)

        stateIsolation.push(TransformFeedbackBindingState(0))

        val offset = first * particleStates.layout.bufferSizeBytes.toLong()
        val size = count * particleStates.layout.bufferSizeBytes.toLong()
        glBindBufferRange(GL_TRANSFORM_FEEDBACK_BUFFER, 0, particleStates.vertexBufferObjectPong, offset, size)

        glBeginTransformFeedback(GL_POINTS)
    }

    open fun dispatchCompute(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount, stateIsolation: StateIsolation): Int {
        glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, QUERY_OBJECT)
        glDrawArrays(GL_POINTS, first, count)
        glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN)

        val written = glGetQueryObjecti(QUERY_OBJECT, GL_QUERY_RESULT)
        return written
    }

    open fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        glEndTransformFeedback()

        glBindBufferRange(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0, 0, 0)

        particleStates.swapBuffers()
    }

    open fun run(particleStates: GpuParticleState, first: Int = 0, count: Int = particleStates.particleCount): Int {
        StateIsolation().use { stateIsolation ->
            bind(particleStates, first, count, stateIsolation)
            val written = dispatchCompute(particleStates, first, count, stateIsolation)
            unbind(particleStates, stateIsolation)
            return written
        }
    }
}