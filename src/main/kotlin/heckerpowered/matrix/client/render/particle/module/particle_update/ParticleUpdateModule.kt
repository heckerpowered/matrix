/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.particle.module.particle_update

import heckerpowered.matrix.client.TimeController.strictDeltaTime
import heckerpowered.matrix.client.render.particle.module.ParticleModule
import heckerpowered.matrix.client.shader.UniformProvider
import org.lwjgl.opengl.GL20.glUniform1f
import kotlin.time.DurationUnit

abstract class ParticleUpdateModule : ParticleModule() {
    companion object {
        val DELTA_TIME_PROVIDER = UniformProvider("DeltaTime") { pointer ->
            glUniform1f(pointer, strictDeltaTime.toDouble(DurationUnit.SECONDS).toFloat())
        }
    }
}