/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.TextureProvider
import heckerpowered.matrix.client.shader.UniformProvider
import net.minecraft.client.Minecraft
import org.joml.Vector4f
import java.io.File

/**
 * The window-sized framebuffer everything is composed into; replaces the former
 * `minecraft.framebuffer` accessor from the pre-26.2 mappings.
 */
val Minecraft.mainRenderTarget: RenderTarget
    get() = gameRenderer.mainRenderTarget()

/** Compatibility accessors matching the pre-26.2 Framebuffer field names. */
val RenderTarget.textureWidth: Int
    get() = width

val RenderTarget.textureHeight: Int
    get() = height

private var primaryFramebuffer: RenderTarget? = null
private var secondaryFramebuffer: RenderTarget? = null
var colorMultiplier = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)

private val colorFusionShader by lazy {
    BlitProgram(
        "post/color_fusion.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0 = colorMultiplier
                putVec4(colorMultiplier)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("primaryFramebuffer") { primaryFramebuffer?.colorTextureView },
            TextureProvider("secondaryFramebuffer") { secondaryFramebuffer?.colorTextureView }
        )
    )
}

private val blendScreenShader by lazy {
    BlitProgram(
        "post/blend_screen.fsh",
        textures = arrayOf(
            TextureProvider("primaryFramebuffer") { primaryFramebuffer?.colorTextureView },
            TextureProvider("secondaryFramebuffer") { secondaryFramebuffer?.colorTextureView }
        )
    )
}

val tentBlurShader by lazy {
    BlitProgram(
        "post/blur/tent.fsh",
        uniforms = arrayOf(
            UniformProvider("MatrixPostUniforms") {
                // MatrixPostData0.x = lod
                putVec4(PostProcessRenderer.levelOfDetail, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
                putVec4(0F, 0F, 0F, 0F)
            }
        ),
        textures = arrayOf(
            TextureProvider("framebuffer", bilinear = true, mipmap = true) { primaryFramebuffer?.colorTextureView }
        )
    )
}

// 1.21 semantics: linearCopyTo/nearestCopyTo were raw glBlitFramebuffer calls — a full
// REPLACE of every destination pixel, black/transparent included. blit_replace.fsh has no
// black-pixel discard (that discard in blit_no_depth.fsh is a compositing behavior); routing
// these copies through the discarding shader left stale destination content behind wherever
// the source was transparent — the source of the fullscreen acrylic-wash leak.
private val linearCopyShader by lazy {
    BlitProgram(
        "blit/blit_replace.fsh",
        uniforms = arrayOf(UniformProvider("BlitConfig") { putFloat(0F) }),
        textures = arrayOf(TextureProvider("framebuffer", bilinear = true) { primaryFramebuffer?.colorTextureView })
    )
}

private val nearestCopyShader by lazy {
    BlitProgram(
        "blit/blit_replace.fsh",
        uniforms = arrayOf(UniformProvider("BlitConfig") { putFloat(0F) }),
        textures = arrayOf(TextureProvider("framebuffer", bilinear = false) { primaryFramebuffer?.colorTextureView })
    )
}

/**
 * Blends this framebuffer with [other] through the color fusion shader into the current
 * post-process target. Renders into the current framebuffer of [PostProcessRenderer].
 */
infix fun RenderTarget.blend(other: RenderTarget) {
    primaryFramebuffer = this
    secondaryFramebuffer = other
    colorFusionShader.drawTo(PostProcessRenderer.currentFramebuffer())
}

infix fun RenderTarget.blendScreen(other: RenderTarget) {
    primaryFramebuffer = this
    secondaryFramebuffer = other
    blendScreenShader.drawTo(PostProcessRenderer.currentFramebuffer())
}

infix fun RenderTarget.copyTo(other: RenderTarget) {
    primaryFramebuffer = this
    linearCopyShader.drawTo(other)
}

infix fun RenderTarget.copyDepthTo(other: RenderTarget) {
    other.copyDepthFrom(this)
}

infix fun RenderTarget.linearCopyTo(other: RenderTarget) {
    primaryFramebuffer = this
    linearCopyShader.drawTo(other)
}

infix fun RenderTarget.nearestCopyTo(other: RenderTarget) {
    primaryFramebuffer = this
    nearestCopyShader.drawTo(other)
}

/**
 * Calculates the recommend number of mipmap levels for a given texture size.
 *
 * Mipmap levels are calculated based on the longest edge (width or height).
 * The number of levels is defined as:
 *
 * `floor(log2(max(width, height))) + 1`
 *
 * This represents the total numbers of mip levels including the base level (origin size).
 * where each subsequent level is half the size of the previous one, until reaching 1x1.
 *
 * This implementation uses bit-level operation to avoid floating-point math,
 * which is faster and more efficient on the JVM.
 *
 * @param width the width of the texture
 * @param height the height of the texture
 * @return the number of mipmap levels needed.
 * @author heckerpowered
 */
fun recommendMipLevel(width: Int, height: Int): Int {
    val size = maxOf(width, height)

    // Explantation:
    // The number of mip levels is: floor(log2(size)) + 1
    //
    // (size - 1).countLeadingZeroBits() gives the number of zero bits before the first 1 bit.
    // For example: 256 (0B100000000) has 23 leading zeros in a 32-bit int.
    //
    // So: Int.SIZE_BITS - (size - 1).countLeadingZeroBits()
    //   = 32 - countLeadingZeroBits(size - 1)
    //   = floor(log2(size)) + 1
    //
    // This avoids using log2 and is equivalent for all positive integers.
    return Int.SIZE_BITS - (size - 1).countLeadingZeroBits()
}

/**
 * Calculates the recommend number of mipmap levels for this framebuffer.
 *
 * Mipmap levels are calculated based on the longest edge (width or height).
 * The number of levels is defined as:
 *
 * `floor(log2(max(width, height))) + 1`
 *
 * This represents the total numbers of mip levels including the base level (origin size).
 * where each subsequent level is half the size of the previous one, until reaching 1x1.
 *
 * This implementation uses bit-level operation to avoid floating-point math,
 * which is faster and more efficient on the JVM.
 *
 * @return the number of mipmap levels needed.
 * @author heckerpowered
 */
fun RenderTarget.recommendMipLevel(): Int {
    return recommendMipLevel(width, height)
}

fun RenderTarget.dump(levelOfDetail: Int = 0, generateMipmap: Boolean = true) {
    dump(hashCode().toString(), levelOfDetail, generateMipmap)
}

/**
 * Writes the color attachment (at [levelOfDetail]) to `screenshots/framebuffer_dump_<name>.png`.
 * Debug utility; reads back through the GpuDevice wrapper so it works on both backends.
 */
fun RenderTarget.dump(name: String, levelOfDetail: Int = 0, @Suppress("UNUSED_PARAMETER") generateMipmap: Boolean = true) {
    val texture = colorTexture ?: return
    val width = texture.getWidth(levelOfDetail)
    val height = texture.getHeight(levelOfDetail)
    if (width <= 0 || height <= 0) {
        return
    }

    val device = RenderSystem.getDevice()
    val encoder = device.createCommandEncoder()
    val bytesPerPixel = texture.format.blockSize()
    val readback = device.createBuffer(
        { "matrix framebuffer dump" },
        GpuBuffer.USAGE_MAP_READ or GpuBuffer.USAGE_COPY_DST,
        (width * height * bytesPerPixel).toLong()
    )
    encoder.copyTextureToBuffer(texture, readback, 0L, {
        readback.map(true, false).use { mapped ->
            NativeImage(width, height, false).use { nativeImage ->
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val color = mapped.data().getInt((y * width + x) * bytesPerPixel)
                        nativeImage.setPixelABGR(x, y, color)
                    }
                }
                // 26.2: NativeImage has no mirror helper anymore; wrapper readback is
                // top-down already, so no flip is needed (debug utility only).

                val file = File("screenshots")
                file.mkdir()

                val filename = "framebuffer_dump_${name}.png"
                nativeImage.writeToFile(File(file, filename))
            }
        }
        readback.close()
    }, levelOfDetail)
}

/**
 * Executes the given [action].
 *
 * Under the 26.2 wrapper API render passes are self-contained: there is no longer any global
 * framebuffer/viewport binding to guard, so this is a plain invocation kept for source
 * compatibility.
 */
@Deprecated("Render passes are self-contained on the 26.2 wrapper API; the guard is no longer needed")
fun framebufferGuard(action: () -> Unit) {
    action()
}
