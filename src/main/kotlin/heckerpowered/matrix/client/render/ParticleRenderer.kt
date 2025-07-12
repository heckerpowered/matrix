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