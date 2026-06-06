/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package net.minecraft.client.gl

open class Framebuffer(
    var textureWidth: Int,
    var textureHeight: Int,
    val useDepthAttachment: Boolean = false,
) {
    var viewportWidth: Int = textureWidth
    var viewportHeight: Int = textureHeight
    var colorAttachment: Int = 0
    var depthAttachment: Int = 0
    var fbo: Int = 0

    fun setClearColor(red: Float, green: Float, blue: Float, alpha: Float) = Unit
    fun resize(width: Int, height: Int, getError: Boolean = false) {
        textureWidth = width
        textureHeight = height
        viewportWidth = width
        viewportHeight = height
    }

    fun clear(getError: Boolean = false) = Unit
    fun beginWrite(setViewport: Boolean = true) = Unit
    fun endWrite() = Unit
    fun draw(width: Int, height: Int, disableBlend: Boolean = false) = Unit
}

open class SimpleFramebuffer(
    width: Int,
    height: Int,
    useDepth: Boolean = false,
    getError: Boolean = false,
) : Framebuffer(width, height, useDepth)
