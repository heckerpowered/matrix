/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.event.PostProcessCallback
import heckerpowered.matrix.client.render.PostProcessRenderer

object PostProcess {
    val emissiveFramebuffer = PostProcessRenderer.createManagedFramebuffer()
    var bloomBrightnessThreshold: Float = 1.0F

    init {
        PostProcessCallback.EVENT.register(::onPostProcess)
    }

    fun onPostProcess() {
        BloomEffect.brightnessPassFramebuffer = emissiveFramebuffer
        BloomEffect.brightnessPassFramebuffer
    }
}