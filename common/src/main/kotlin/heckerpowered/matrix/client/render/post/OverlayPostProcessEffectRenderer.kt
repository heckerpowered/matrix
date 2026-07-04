/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.mainRenderTarget
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.DeltaTracker
import java.time.Duration

object OverlayPostProcessEffectRenderer {

    private val framebuffer = PostProcessRenderer.createManagedFramebuffer()
    private val dissolveAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(1000))
    private var previousState = false

    init {
        // HudRenderCallback.EVENT.register(::onHudRender)
    }

    private fun onHudRender(drawContext: GuiGraphicsExtractor, renderTickCounter: DeltaTracker) {
        if (minecraft.gui.screen() == null) {
            dissolveAnimation.value = 1.0
            previousState = false
        } else if (!previousState) {
            previousState = true
            dissolveAnimation.value = .0
        }
    }

    // 26.2: beginWrite(false) used to redirect subsequent vanilla-overlay draws into `framebuffer`;
    // the vanilla-supported equivalent is the RenderSystem output-texture override (see
    // heckerpowered.matrix.client.core.FramebufferSpoof for the established idiom). endRenderOverlay
    // clears the override before compositing.
    @JvmStatic
    fun beginRenderOverlay() {
        PostProcessRenderer.clear(framebuffer)
        RenderSystem.outputColorTextureOverride = framebuffer.colorTextureView
        RenderSystem.outputDepthTextureOverride = framebuffer.depthTextureView
    }

    @JvmStatic
    fun endRenderOverlay() {
        RenderSystem.outputColorTextureOverride = null
        RenderSystem.outputDepthTextureOverride = null

        TextureDissolveShader.colorAttachment = framebuffer.colorTextureView
        TextureDissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        TextureDissolveShader.program.drawTo(minecraft.mainRenderTarget)
    }
}