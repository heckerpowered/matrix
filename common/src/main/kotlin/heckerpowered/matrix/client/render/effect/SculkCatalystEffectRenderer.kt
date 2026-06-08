/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.effect

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.LivingEntity
import java.time.Duration

object SculkCatalystEffectRenderer {
    var entity: LivingEntity? = null
        set(value) {
            if (value == null) {
                volumeRadius.value = .0
            } else if (value !== field) {
                volumeRadius.from = .0
                volumeRadius.to = 16.0
                volumeRadius.duration = Duration.ofMillis(300)
                volumeRadius.start()

                emissiveStrength.from = .0
                emissiveStrength.to = 4.0
                emissiveStrength.duration = Duration.ofMillis(300)
                emissiveStrength.start()

                ShockwaveRenderer.wavePosition = value.position().toVector3f()
                ShockwaveRenderer.waveRadius.from = .0
                ShockwaveRenderer.waveRadius.to = 16.0
                ShockwaveRenderer.waveRadius.duration = Duration.ofMillis(500)
                ShockwaveRenderer.waveRadius.start()

                ShockwaveRenderer.waveSize.from = 0.5
                ShockwaveRenderer.waveSize.to = .0
                ShockwaveRenderer.waveSize.duration = Duration.ofMillis(500)
                ShockwaveRenderer.waveSize.start()
            }
            field = value
        }

    val volumeRadius = SimpleDoubleAnimation()
    val emissiveStrength = SimpleDoubleAnimation()

    fun render() {
        val entity = entity ?: return
        if (!entity.isAlive) {
            this.entity = null
            return
        }

        if (entity.tickCount % 2 != 0) {
            return
        }

        val level = minecraft.level ?: return
        val position = entity.position().add(.0, entity.boundingBox.ysize * 0.5, .0)
        level.addParticle(
            ParticleTypes.WITCH,
            position.x,
            position.y,
            position.z,
            .0,
            0.03,
            .0,
        )
    }
}
