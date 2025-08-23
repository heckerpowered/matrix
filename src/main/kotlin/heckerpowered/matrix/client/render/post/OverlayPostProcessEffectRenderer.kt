/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import java.time.Duration

object OverlayPostProcessEffectRenderer {

    private val framebuffer = PostProcessRenderer.createManagedFramebuffer()
    private val dissolveAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(1000))
    private var previousState = false

    init {
        // HudRenderCallback.EVENT.register(::onHudRender)
    }

    private fun onHudRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        if (minecraft.currentScreen == null) {
            dissolveAnimation.value = 1.0
            previousState = false
        } else if (!previousState) {
            previousState = true
            dissolveAnimation.value = .0
        }
    }

    @JvmStatic
    fun beginRenderOverlay() {
        framebuffer.beginWrite(false)
    }

    @JvmStatic
    fun endRenderOverlay() {
        TextureDissolveShader.colorAttachment = framebuffer.colorAttachment
        TextureDissolveShader.dissolveFactor = dissolveAnimation.animatedValue.toFloat()
        TextureDissolveShader.program.enableShader()
        BlitProgram.blit()
        TextureDissolveShader.program.disableShader()
        RenderSystem.disableBlend()
        minecraft.framebuffer.beginWrite(false)
    }
}