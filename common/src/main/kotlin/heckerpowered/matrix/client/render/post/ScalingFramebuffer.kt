/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.render.textureHeight
import heckerpowered.matrix.client.render.textureWidth
import heckerpowered.matrix.core.FramebufferExtension
import heckerpowered.matrix.core.approximatelyEqual
import com.mojang.blaze3d.pipeline.TextureTarget
import kotlin.math.floor

class ScalingFramebuffer(label: String, width: Int, height: Int, useDepth: Boolean, private val resolutionScaling: Double) :
    TextureTarget(label, width, height, useDepth, FramebufferExtension.framebufferColorFormat) {
    val actualWidth
        get() = floor(textureWidth * resolutionScaling).toInt()

    val actualHeight
        get() = floor(textureHeight * resolutionScaling).toInt()

    init {
        resize(width, height)
    }

    override fun resize(width: Int, height: Int) {
        if (resolutionScaling.approximatelyEqual(.0)) {
            super.resize(width, height)
            return
        }

        val scaledWidth = floor(width * resolutionScaling).toInt().coerceAtLeast(1)
        val scaledHeight = floor(height * resolutionScaling).toInt().coerceAtLeast(1)
        super.resize(scaledWidth, scaledHeight)
    }
}