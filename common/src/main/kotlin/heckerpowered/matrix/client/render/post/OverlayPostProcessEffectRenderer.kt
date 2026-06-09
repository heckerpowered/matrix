/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import net.minecraft.client.DeltaTracker
import java.time.Duration

object OverlayPostProcessEffectRenderer {
    private val framebuffer = PostProcessRenderer.createManagedFramebuffer()
    private val dissolveAnimation = heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation(
        initValue = 1.0,
        duration = Duration.ofMillis(1000),
    )
    private var previousState = false

    fun render(tickCounter: DeltaTracker) {
        if (minecraft.gui.screen() == null) {
            dissolveAnimation.value = 1.0
            previousState = false
        } else if (!previousState) {
            previousState = true
            dissolveAnimation.value = .0
        }
    }

    @JvmStatic
    fun beginRenderOverlay() {
        framebuffer.clear(false)
    }

    @JvmStatic
    fun endRenderOverlay() {
        TextureDissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        val output = PostProcessRenderer.currentFramebuffer()
        PostProcessRenderer.renderShaderToFramebuffer(
            TextureDissolveShader.program,
            output,
            mapOf("colorAttachment" to framebuffer),
        )
        PostProcessRenderer.copyFramebuffer(output, PostProcessRenderer.sourceFramebuffer)
        PostProcessRenderer.nextFramebuffer()
    }
}
