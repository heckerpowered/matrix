/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle

import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.particle.module.ParticleModule
import heckerpowered.matrix.client.render.particle.module.ParticleRenderModule
import heckerpowered.matrix.client.render.particle.module.particle_spawn.ParticleSpawnModule
import heckerpowered.matrix.client.render.particle.module.particle_update.ParticleUpdateModule

class ParticleSystem(
    val particleCount: Int,
    val particleSpawnModules: Array<ParticleSpawnModule> = emptyArray(),
    val particleUpdateModules: Array<ParticleUpdateModule> = emptyArray(),
    val particleRenderModules: Array<ParticleRenderModule> = emptyArray(),
    val layout: MemoryLayout,
) {
    val particleStates = GpuParticleState.createGpuParticleState(particleCount, layout)
    var activeParticleCount = 0
        private set

    private fun clampRange(first: Int, count: Int, total: Int): Pair<Int, Int>? {
        val first = first.coerceIn(0..total)
        val count = count.coerceIn(0, total - first)
        return if (count > 0) first to count else null
    }

    private inline fun <T : ParticleModule> runPartial(
        first: Int, count: Int, modules: Array<out T>,
        action: T.(particleStates: GpuParticleState, first: Int, count: Int) -> Int,
    ): Int {
        clampRange(first, count, particleCount)?.let { (first, count) ->
            var written = count
            for (module in modules) {
                written = module.action(particleStates, first, written)
            }
            return written
        }
        return 0
    }

    fun spawnParticles() {
        spawnPartialParticles(0, particleCount)
    }

    fun spawnPartialParticles(first: Int, count: Int) {
        runPartial(first, count, particleSpawnModules) { particleStates, first, count ->
            run(particleStates, first, count)
        }
        activeParticleCount = particleCount
    }

    fun updateParticles() {
        updatePartialParticles(0, activeParticleCount)
    }

    fun updatePartialParticles(first: Int, count: Int) {
        activeParticleCount = runPartial(first, count, particleUpdateModules) { particleStates, first, count ->
            run(particleStates, first, count)
        }
    }

    fun renderParticles() {
        for (particleRenderModules in particleRenderModules) {
            particleRenderModules.run(particleStates, 0, activeParticleCount)
        }
    }

    fun renderPartialParticles(first: Int, count: Int) {
        clampRange(first, count, particleCount)?.let { (first, count) ->
            for (mod in particleRenderModules) {
                mod.run(particleStates, first, count)
            }
        }
    }
}
