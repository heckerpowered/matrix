/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.effect

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.mainRenderTarget
import heckerpowered.matrix.client.render.shader.VolumeDistortion
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import net.minecraft.world.entity.LivingEntity

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
        // 1.21 sampled the main framebuffer's own color attachment while drawing back into
        // it, which Vulkan forbids (same-texture read/write); snapshot the scene into the
        // post-process ping target and sample the copy instead. Main's depth stays bound
        // directly: this pass writes color only (writesDepth = false), so the depth view is
        // never attached to the pass and reading it is safe on both backends.
        PostProcessRenderer.copyFramebuffer(minecraft.mainRenderTarget, PostProcessRenderer.ping)
        VolumeDistortion.depthAttachment = minecraft.mainRenderTarget.depthTextureView
        VolumeDistortion.sceneColorTexture = PostProcessRenderer.ping.colorTextureView

        VolumeDistortion.volumeRadius = volumeRadius.animatedValue.toFloat()
        VolumeDistortion.emissiveStrength = emissiveStrength.animatedValue.toFloat()

        val tickDelta = minecraft.deltaTracker.getGameTimeDeltaPartialTick(false)
        VolumeDistortion.volumePosition = entity.getPosition(tickDelta).toVector3f()

        VolumeDistortion.Shader.drawTo(minecraft.mainRenderTarget)
    }
}