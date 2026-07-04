/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.core

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem

object FramebufferSpoof {
    private val stack = mutableListOf<RenderTarget>()

    private var spoofedFramebuffer: RenderTarget? = null

    @JvmStatic
    fun getSpoofedFramebuffer(): RenderTarget? {
        return spoofedFramebuffer
    }

    fun push(spoofedFramebuffer: RenderTarget) {
        FramebufferSpoof.spoofedFramebuffer?.let {
            stack.addLast(it)
        }

        FramebufferSpoof.spoofedFramebuffer = spoofedFramebuffer
        applyOutputOverride(spoofedFramebuffer)
    }

    fun pop() {
        if (stack.isEmpty()) {
            spoofedFramebuffer = null
            applyOutputOverride(null)
            return
        }

        spoofedFramebuffer = stack.removeLast()
        applyOutputOverride(spoofedFramebuffer)
    }

    fun clear() {
        stack.clear()
        spoofedFramebuffer = null
        applyOutputOverride(null)
    }

    // 26.2: there is no global framebuffer binding anymore; the vanilla-supported
    // equivalent of beginWrite is redirecting RenderType draws via the output overrides.
    private fun applyOutputOverride(target: RenderTarget?) {
        RenderSystem.outputColorTextureOverride = target?.colorTextureView
        RenderSystem.outputDepthTextureOverride = target?.depthTextureView
    }
}