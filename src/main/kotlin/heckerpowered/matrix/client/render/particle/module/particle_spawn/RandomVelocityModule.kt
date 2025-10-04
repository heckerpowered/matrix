/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.module.particle_spawn

import heckerpowered.matrix.client.render.particle.GpuParticleState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.shader.Program
import heckerpowered.matrix.client.shader.ResourceShader
import heckerpowered.matrix.client.shader.UniformProvider
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.*

class RandomVelocityModule : ParticleSpawnModule() {
    companion object {
        private val program = Program(
            ResourceShader("/assets/matrix/shaders/particle/particle_spawn/random_velocity.vsh", GL_VERTEX_SHADER),
            uniforms = arrayOf(
                UniformProvider("time") { pointer ->
                    glUniform1f(pointer, (System.currentTimeMillis() % 10000) / 1000F)
                },
                UniformProvider("multiplier") { pointer ->
                    glUniform3f(pointer, multiplier.x, multiplier.y, multiplier.z)
                },
                UniformProvider("speedRange") { pointer ->
                    glUniform2f(pointer, speedRange.x, speedRange.y)
                }),
            components = arrayOf(TRANSFORM_FEEDBACK)
        )
        var multiplier = Vector3f(1.0F, 1.0F, 1.0F)
        var speedRange = Vector2f(0.0F, 1.0F)
    }

    var multiplier = Vector3f(1.0F, 1.0F, 1.0F)
    var speedRange = Vector2f(0.0F, 1.0F)
    override fun bind(particleStates: GpuParticleState, first: Int, count: Int, stateIsolation: StateIsolation) {
        RandomVelocityModule.multiplier = multiplier
        RandomVelocityModule.speedRange = speedRange
        program.enableShader()
        super.bind(particleStates, first, count, stateIsolation)
    }

    override fun unbind(particleStates: GpuParticleState, stateIsolation: StateIsolation) {
        super.unbind(particleStates, stateIsolation)
        program.disableShader()
    }
}
