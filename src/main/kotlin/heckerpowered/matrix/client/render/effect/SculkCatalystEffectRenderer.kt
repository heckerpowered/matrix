/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.effect

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.shader.VolumeDistortion
import heckerpowered.matrix.client.render.state.BlendFuncSeparateState
import heckerpowered.matrix.client.render.state.FramebufferState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.ViewportState
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import net.minecraft.entity.LivingEntity

object SculkCatalystEffectRenderer {
    var entity: LivingEntity? = null
        set(value) {
            if (value == null) {
                volumeRadius.value = 0.0
            } else if (value != field) {
                volumeRadius.from = .0
                volumeRadius.to = 16.0
                volumeRadius.start()

                emissiveStrength.from = 0.0
                emissiveStrength.to = 4.0
                emissiveStrength.start()
            }
            field = value
        }
    val volumeRadius = SimpleDoubleAnimation()
    val emissiveStrength = SimpleDoubleAnimation()

    fun render() {
        val entity = this.entity ?: return
        if (!entity.isAlive) {
            this.entity = null
            return
        }

        VolumeDistortion.grayscaleIntensity = 1.0F
        VolumeDistortion.depthAttachment = minecraft.framebuffer.depthAttachment
        VolumeDistortion.sceneColorTexture = minecraft.framebuffer.colorAttachment

        VolumeDistortion.volumeRadius = volumeRadius.animatedValue.toFloat()
        VolumeDistortion.emissiveStrength = emissiveStrength.animatedValue.toFloat()

        val tickDelta = minecraft.renderTickCounter.getTickDelta(false)
        VolumeDistortion.volumePosition = entity.getLerpedPos(tickDelta).toVector3f()

        StateIsolation.isolate(
            FramebufferState(minecraft.framebuffer), ViewportState(minecraft.framebuffer),
            BlendState.captureSnapshot(), BlendFuncSeparateState.captureSnapshot()
        ) {
            VolumeDistortion.Shader.blit()
        }
    }
}