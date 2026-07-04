/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.buffers.Std140Builder
import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.client.render.MatrixRenderSystem
import org.joml.Matrix4f
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Writes one std140 uniform block of a [BlitProgram].
 *
 * [name] is the uniform block's name as declared in the GLSL source
 * (`layout(std140) uniform <name> { ... };`); [write] emits the block's members in
 * declaration order through [Std140Builder], which handles std140 alignment. This replaces
 * the former per-uniform `glUniform*` writers: on the 26.2 wrapper API (Vulkan and OpenGL
 * alike) loose uniforms no longer exist, uniform data lives in buffer-backed blocks.
 */
open class UniformProvider(val name: String, val write: Std140Builder.() -> Unit)

/**
 * Supplies a texture for the sampler uniform called [name] in a [BlitProgram].
 * Returning `null` skips the binding for this draw.
 */
open class TextureProvider(
    val name: String,
    val bilinear: Boolean = false,
    val mipmap: Boolean = false,
    val view: () -> GpuTextureView?,
)

/** Seconds-scale time value matching the previous `time` uniform providers. */
fun shaderTimeSeconds(): Float {
    return System.nanoTime().milliseconds.toDouble(DurationUnit.SECONDS).toFloat()
}

/** Camera-relative matrices, kept for shaders whose blocks embed them. */
fun Std140Builder.putProjectionMatrix(): Std140Builder = putMat4f(projectionMatrix)

fun Std140Builder.putInverseProjectionMatrix(): Std140Builder = putMat4f(Matrix4f(projectionMatrix).invert())

fun Std140Builder.putViewMatrix(): Std140Builder = putMat4f(MatrixRenderSystem.viewMatrix)

fun Std140Builder.putInverseViewMatrix(): Std140Builder = putMat4f(MatrixRenderSystem.inverseViewMatrix)

fun Std140Builder.putViewProjectionMatrix(): Std140Builder = putMat4f(MatrixRenderSystem.viewProjectionMatrix)

/** Interpolated local player position, matching the previous `playerPosition` provider. */
fun Std140Builder.putPlayerPosition(): Std140Builder {
    val tickDelta = minecraft.deltaTracker.getGameTimeDeltaPartialTick(true)
    val position = minecraft.player?.getPosition(tickDelta)
    return putVec3(
        position?.x?.toFloat() ?: 0F,
        position?.y?.toFloat() ?: 0F,
        position?.z?.toFloat() ?: 0F
    )
}

/** Camera position, matching the previous `cameraPosition` provider. */
fun Std140Builder.putCameraPosition(): Std140Builder {
    val position = minecraft.gameRenderer.mainCamera().position()
    return putVec3(position.x.toFloat(), position.y.toFloat(), position.z.toFloat())
}

/** Framebuffer resolution, matching the previous `resolution` provider. */
fun Std140Builder.putResolution(): Std140Builder {
    return putVec2(
        minecraft.window.width.toFloat(),
        minecraft.window.height.toFloat()
    )
}
