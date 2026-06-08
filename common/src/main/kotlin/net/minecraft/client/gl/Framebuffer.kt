/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package net.minecraft.client.gl

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.resource.RenderTargetDescriptor
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import net.minecraft.client.Minecraft
import kotlin.math.roundToInt

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

    private var clearColor: Int = 0
    private var backingTarget: RenderTarget? = null

    open val renderTarget: RenderTarget
        get() {
            val existing = backingTarget
            if (existing != null) {
                return existing
            }

            val allocated = RenderTargetDescriptor(
                textureWidth.coerceAtLeast(1),
                textureHeight.coerceAtLeast(1),
                useDepthAttachment,
                clearColor,
            ).allocate()
            backingTarget = allocated
            return allocated
        }

    val colorTexture: GpuTexture
        get() = renderTarget.colorTexture ?: error("Framebuffer color texture has not been created")

    val colorTextureView: GpuTextureView
        get() = renderTarget.colorTextureView ?: error("Framebuffer color texture view has not been created")

    val depthTexture: GpuTexture?
        get() = renderTarget.depthTexture

    val depthTextureView: GpuTextureView?
        get() = renderTarget.depthTextureView

    fun setClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        clearColor = argb(alpha, red, green, blue)
    }

    open fun resize(width: Int, height: Int, getError: Boolean = false) {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        textureWidth = safeWidth
        textureHeight = safeHeight
        viewportWidth = safeWidth
        viewportHeight = safeHeight
        backingTarget?.resize(safeWidth, safeHeight)
    }

    fun clear(getError: Boolean = false) {
        val target = renderTarget
        val encoder = RenderSystem.getDevice().createCommandEncoder()
        val depth = target.depthTexture
        if (useDepthAttachment && depth != null) {
            encoder.clearColorAndDepthTextures(colorTexture, clearColor, depth, 0.0)
        } else {
            encoder.clearColorTexture(colorTexture, clearColor)
        }
    }

    fun beginWrite(setViewport: Boolean = true) {
        RenderSystem.outputColorTextureOverride = colorTextureView
        RenderSystem.outputDepthTextureOverride = depthTextureView
    }

    fun endWrite() {
        RenderSystem.outputColorTextureOverride = null
        RenderSystem.outputDepthTextureOverride = null
    }

    fun draw(width: Int, height: Int, disableBlend: Boolean = false) {
        val mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget()
        renderTarget.blitAndBlendToTexture(
            mainTarget.colorTextureView ?: error("Main render target color texture view has not been created"),
            mainTarget.depthTextureView ?: error("Main render target depth texture view has not been created"),
        )
    }

    fun destroyBuffers() {
        backingTarget?.destroyBuffers()
        backingTarget = null
    }

    private fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
        val a = (alpha.coerceIn(0.0F, 1.0F) * 255.0F).roundToInt()
        val r = (red.coerceIn(0.0F, 1.0F) * 255.0F).roundToInt()
        val g = (green.coerceIn(0.0F, 1.0F) * 255.0F).roundToInt()
        val b = (blue.coerceIn(0.0F, 1.0F) * 255.0F).roundToInt()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

open class SimpleFramebuffer(
    width: Int,
    height: Int,
    useDepth: Boolean = false,
    getError: Boolean = false,
) : Framebuffer(width, height, useDepth)
