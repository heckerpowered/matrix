/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.shader.BlitProgram
import heckerpowered.matrix.client.shader.ResourceShader
import net.minecraft.client.gl.Framebuffer
import org.joml.Vector4f
import org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT
import org.lwjgl.opengl.GL11.GL_LINEAR
import org.lwjgl.opengl.GL11.GL_NEAREST
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

var colorMultiplier = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
private val colorFusionShader by lazy {
    BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/color_fusion.fsh", GL_FRAGMENT_SHADER),
    )
}

private val blendScreenShader by lazy {
    BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/blend_screen.fsh", GL_FRAGMENT_SHADER),
    )
}

val tentBlurShader = BlitProgram(
    ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
    ResourceShader("/assets/matrix/shaders/post/blur/tent.fsh", GL_FRAGMENT_SHADER),
)

fun Framebuffer.blit(target: Framebuffer, mask: Int, filter: Int) {
    if ((mask and GL_DEPTH_BUFFER_BIT) != 0) {
        PostProcessRenderer.copyDepthFramebuffer(this, target)
    } else {
        PostProcessRenderer.copyFramebuffer(this, target)
    }
}

infix fun Framebuffer.blend(other: Framebuffer) {
    PostProcessRenderer.renderShaderToFramebuffer(
        colorFusionShader,
        PostProcessRenderer.currentFramebuffer(),
        mapOf("primaryFramebuffer" to this, "secondaryFramebuffer" to other),
    )
    PostProcessRenderer.nextFramebuffer()
}

infix fun Framebuffer.blendScreen(other: Framebuffer) {
    PostProcessRenderer.renderShaderToFramebuffer(
        blendScreenShader,
        PostProcessRenderer.currentFramebuffer(),
        mapOf("primaryFramebuffer" to this, "secondaryFramebuffer" to other),
    )
    PostProcessRenderer.nextFramebuffer()
}

infix fun Framebuffer.copyTo(other: Framebuffer) {
    PostProcessRenderer.copyFramebuffer(this, other)
}

infix fun Framebuffer.copyDepthTo(other: Framebuffer) {
    PostProcessRenderer.copyDepthFramebuffer(this, other)
}

infix fun Framebuffer.linearCopyTo(other: Framebuffer) {
    PostProcessRenderer.copyFramebuffer(this, other)
}

infix fun Framebuffer.nearestCopyTo(other: Framebuffer) {
    PostProcessRenderer.copyFramebuffer(this, other)
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
fun Framebuffer.recommendMipLevel(): Int {
    return recommendMipLevel(textureWidth, textureHeight)
}

fun Framebuffer.dump(levelOfDetail: Int = 0, generateMipmap: Boolean = true) {
    dump(hashCode().toString(), levelOfDetail, generateMipmap)
}

fun Framebuffer.dump(name: String, levelOfDetail: Int = 0, generateMipmap: Boolean = true) {
}

/**
 * Executes the given [action] while preserving the currently bound framebuffer and viewport state.
 *
 * This function backs up the current framebuffer binding and viewport dimensions,
 * then invokes the specified [action]. After the action completes, the original framebuffer
 * and viewport state are restored. This ensures that temporary framebuffer or viewport changes
 * inside [action] do not leak outside the function scope.
 *
 * This is especially useful when performing off-screen rendering or rendering to custom framebuffers,
 * as it guarantees rendering state isolation.
 *
 * Example usage:
 * ```
 * framebufferGuard {
 *     glBindFramebuffer(GL_FRAMEBUFFER, customFramebuffer)
 *     GlStateManager._viewport(0, 0, width, height)
 *     renderSomething()
 * }
 * // Original framebuffer and viewport are now restored
 * ```
 *
 * @param action The block of code to execute within the guarded framebuffer and viewport state.
 */
@Deprecated("Use StateIsolation.isolate(FramebufferState(this)) { action() } instead")
fun framebufferGuard(action: () -> Unit) {
    action()
}
