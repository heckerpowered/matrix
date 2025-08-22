/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.render.particle.ParticleSystem

object ParticleRenderer {
    val particleSystems = listOf<ParticleSystem>()

    fun renderParticles() {
        for (particleSystem in particleSystems) {
            particleSystem.renderParticles()
        }
    }

    fun destroyParticles() {

    }
}