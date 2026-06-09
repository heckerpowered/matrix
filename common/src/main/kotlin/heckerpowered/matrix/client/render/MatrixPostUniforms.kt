/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.systems.CommandEncoder
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.projectionMatrix
import heckerpowered.matrix.client.render.post.BloomEffect
import heckerpowered.matrix.client.render.post.CollapseEffectRenderer
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.client.render.post.ShockwaveRenderer
import heckerpowered.matrix.client.render.post.ToneMapping
import heckerpowered.matrix.client.render.shader.GaussianBlurRenderer
import heckerpowered.matrix.client.render.shader.RadialBlurRenderer
import heckerpowered.matrix.client.render.shader.TentShader
import heckerpowered.matrix.client.render.shader.VolumeDistortion
import heckerpowered.matrix.client.render.shader.hud.ProgressRingRenderer
import heckerpowered.matrix.client.render.shader.sdf.DropShadowRenderer
import heckerpowered.matrix.client.render.shader.sdf.SignedDistanceField
import heckerpowered.matrix.client.render.post.TextureDissolveShader
import heckerpowered.matrix.client.shader.TexturePixelDissolveProgram
import heckerpowered.matrix.client.viewMatrix
import net.minecraft.client.gl.Framebuffer
import org.joml.Matrix4f
import org.joml.Vector4f
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MatrixPostUniforms {
    const val BLOCK_NAME = "MatrixPostUniforms"
    private const val BUFFER_SIZE = 256L

    var colorFilterColor = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
    var edgeHighlightThreshold = 1.0F
    var edgeHighlightColor = Vector4f(0.7F, 0.1F, 0.1F, 1.0F)
    var auraAlpha = .0F
    var auraColor = Vector4f(.0F, .0F, .0F, .0F)
    var grainStrength = 0.05F

    private val buffers = mutableMapOf<String, GpuBuffer>()

    fun prepare(
        pass: MatrixShaderPipelines.PostProcessPass,
        input: Framebuffer,
        output: Framebuffer,
        encoder: CommandEncoder,
    ): Map<String, GpuBufferSlice> {
        if (pass.uniformBlocks.isEmpty()) {
            return emptyMap()
        }

        val result = linkedMapOf<String, GpuBufferSlice>()
        for (uniformBlock in pass.uniformBlocks) {
            val data = ByteBuffer.allocateDirect(BUFFER_SIZE.toInt()).order(ByteOrder.nativeOrder())
            write(uniformBlock, pass.fragmentShader, input, output, data)
            data.position(0)
            data.limit(BUFFER_SIZE.toInt())

            val buffer = buffers.getOrPut(uniformBlock) {
                RenderSystem.getDevice().createBuffer(
                    { "Matrix post uniform $uniformBlock" },
                    GpuBuffer.USAGE_UNIFORM or GpuBuffer.USAGE_COPY_DST,
                    BUFFER_SIZE,
                )
            }
            val slice = buffer.slice(0, BUFFER_SIZE)
            encoder.writeToBuffer(slice, data)
            result[uniformBlock] = slice
        }
        return result
    }

    private fun write(block: String, fragmentShader: String, input: Framebuffer, output: Framebuffer, data: ByteBuffer) {
        if (block != BLOCK_NAME) {
            return
        }

        when (fragmentShader) {
            "post/hud/progress_ring" -> {
                ProgressRingRenderer.center.also { center ->
                    data.putVec4(0, ProgressRingRenderer.progress, ProgressRingRenderer.radius, ProgressRingRenderer.thickness, .0F)
                    data.putVec4(1, center.x, center.y, .0F, .0F)
                    ProgressRingRenderer.color.also { color -> data.putVec4(2, color.x, color.y, color.z, color.w) }
                }
            }

            "post/tone_mapping/aces_filmic" -> {
                data.putVec4(0, ToneMapping.exposureLinear, ToneMapping.exposureEv, .0F, .0F)
            }

            "post/bloom/bloom_brightness_pass" -> {
                data.putVec4(0, BloomEffect.brightnessThreshold, BloomEffect.bloomIntensity, .0F, .0F)
            }

            "post/blur/radial_blur" -> {
                data.putVec4(0, RadialBlurRenderer.strength, RadialBlurRenderer.samples.toFloat(), .0F, .0F)
            }

            "post/blur/tent" -> {
                data.putVec4(0, PostProcessRenderer.levelOfDetail + TentShader.levelOfDetail, .0F, .0F, .0F)
            }

            "post/blur/kawase_blur" -> {
                data.putVec4(0, 1.0F, 1.0F, .0F, .0F)
            }

            "post/blur/gaussian_blur" -> {
                val kernel = GaussianBlurRenderer.gaussianKernel
                val kernelSize = (kernel.size - 1).coerceIn(0, 48)
                data.putVec4(0, GaussianBlurRenderer.direction.x, GaussianBlurRenderer.direction.y, kernelSize.toFloat(), .0F)
                if (kernel.isEmpty()) {
                    data.putFloat(16, 1.0F)
                } else {
                    for (index in 0..kernelSize) {
                        data.putFloat(16 + index * 4, kernel[index])
                    }
                }
            }

            "post/bloom/bloom_lower_sampling_pass" -> {
                data.putVec4(0, 1.0F, .0F, .0F, .0F)
            }

            "post/bloom/bloom_super_sampling_pass" -> {
                data.putVec4(0, 15.0F, .0F, .0F, .0F)
            }

            "post/lower_sampling/lod" -> {
                data.putVec4(0, ScaleSampling.levelOfDetail, .0F, .0F, .0F)
            }

            "post/lower_sampling/hybrid_lod" -> {
                data.putVec4(0, ScaleSampling.levelOfDetail, ScaleSampling.levelOfDetail + 1.0F, 0.5F, .0F)
            }

            "post/sampling/bilinear" -> {
                data.putVec4(
                    0,
                    input.textureWidth.toFloat(),
                    input.textureHeight.toFloat(),
                    output.textureWidth.toFloat(),
                    output.textureHeight.toFloat(),
                )
            }

            "post/sdf/jump_flooding" -> {
                data.putVec4(0, SignedDistanceField.stepSize, .0F, .0F, .0F)
            }

            "post/sdf/drop_shadow" -> {
                DropShadowRenderer.shadowOffset.also { offset ->
                    data.putVec4(0, offset.x, offset.y, DropShadowRenderer.shadowSize, .0F)
                }
                DropShadowRenderer.shadowColor.also { color ->
                    data.putVec4(1, color.x, color.y, color.z, color.w)
                }
            }

            "post/color/colorful" -> {
                data.putVec4(0, 1.0F, 1.0F, 1.0F, .0F)
            }

            "post/color_filter" -> {
                colorFilterColor.also { color ->
                    data.putVec4(0, color.x, color.y, color.z, color.w)
                }
            }

            "post/dissolve/dissolve" -> {
                val dissolveFactor = TexturePixelDissolveProgram.dissolveFactor.takeIf { it != .0F } ?: 0.5F
                data.putVec4(0, dissolveFactor, 0.05F, 15.0F, 16.0F)
                data.putVec4(1, 1.0F, currentTimeSeconds(), input.textureWidth.toFloat(), input.textureHeight.toFloat())
                data.putVec4(2, 0.1F, 0.5F, 1.0F, 1.0F)
            }

            "post/dissolve/texture_dissolve" -> {
                val dissolveFactor = TextureDissolveShader.dissolveFactor
                    .takeIf { it != .0F }
                    ?: TexturePixelDissolveProgram.dissolveFactor.takeIf { it != .0F }
                    ?: 0.5F
                data.putVec4(0, dissolveFactor, 0.05F, 16.0F, 1.0F)
                data.putVec4(1, .0F, 0.5F, 1.0F, 1.0F)
            }

            "post/dissolve/texture_pixel_dissolve" -> {
                val dissolveFactor = TexturePixelDissolveProgram.dissolveFactor.takeIf { it != .0F } ?: 0.5F
                data.putVec4(0, dissolveFactor, 0.05F, 100.0F, 1.0F)
                data.putVec4(1, .0F, 0.5F, 1.0F, 1.0F)
            }

            "post/color_fusion" -> {
                data.putVec4(0, colorMultiplier.x, colorMultiplier.y, colorMultiplier.z, colorMultiplier.w)
            }

            "post/edge_highlight" -> {
                data.putVec4(0, edgeHighlightThreshold, .0F, .0F, .0F)
                edgeHighlightColor.also { color ->
                    data.putVec4(1, color.x, color.y, color.z, color.w)
                }
            }

            "post/aura" -> {
                data.putVec4(0, currentTimeSeconds(), auraAlpha, .0F, .0F)
                auraColor.also { color -> data.putVec4(1, color.x, color.y, color.z, color.w) }
            }

            "post/circle" -> {
                data.putMatrix4f(0, viewMatrix)
                data.putMatrix4f(64, projectionMatrix)
                data.putVec4(8, 600.0F, 600.0F, 600.0F, 300.0F)
                data.putVec4(9, input.textureWidth.toFloat(), input.textureHeight.toFloat(), 1.0F, .0F)
            }

            "post/ghost" -> {
                data.putVec4(0, 1.0F, .0F, .0F, .0F)
            }

            "post/grain/background_grain" -> {
                data.putVec4(0, grainStrength, .0F, .0F, .0F)
            }

            "post/the_world" -> {
                data.putVec4(0, MatrixHud.grayscaleIntensity, .0F, .0F, .0F)
            }

            "post/volume_distortion" -> {
                data.putMatrix4f(0, viewMatrix.invert(Matrix4f()))
                data.putMatrix4f(64, projectionMatrix.invert(Matrix4f()))
                VolumeDistortion.volumePosition.also { position ->
                    data.putVec4(8, position.x, position.y, position.z, VolumeDistortion.volumeRadius)
                }
                data.putVec4(9, VolumeDistortion.grayscaleIntensity, VolumeDistortion.emissiveStrength, .0F, .0F)
            }

            "post/shockwave" -> {
                data.putMatrix4f(0, projectionMatrix.invert(Matrix4f()))
                data.putMatrix4f(64, viewMatrix.invert(Matrix4f()))
                ShockwaveRenderer.wavePosition.also { position ->
                    data.putVec4(8, position.x, position.y, position.z, ShockwaveRenderer.waveRadius.animatedValue.toFloat())
                }
                ShockwaveRenderer.waveColor.also { color ->
                    data.putVec4(9, color.x, color.y, color.z, color.w)
                }
                data.putVec4(10, ShockwaveRenderer.waveSize.animatedValue.toFloat(), .0F, .0F, .0F)
            }

            "post/collapse" -> {
                data.putMatrix4f(0, projectionMatrix.invert(Matrix4f()))
                data.putMatrix4f(64, viewMatrix.invert(Matrix4f()))
                val playerPosition = minecraft.player?.position()
                data.putVec4(
                    8,
                    playerPosition?.x?.toFloat() ?: .0F,
                    playerPosition?.y?.toFloat() ?: .0F,
                    playerPosition?.z?.toFloat() ?: .0F,
                    CollapseEffectRenderer.dissolveFactor.animatedValue.toFloat(),
                )
                data.putVec4(9, input.textureWidth.toFloat(), input.textureHeight.toFloat(), .0F, .0F)
            }

            "post/vortex/vortex" -> {
                data.putVec4(0, currentTimeSeconds(), 0.4F, 0.5F, .0F)
            }

            "post/vortex/inverse_vortex" -> {
                data.putVec4(0, currentTimeSeconds(), 1.0F, 1.0F, 0.1F)
            }
        }
    }

    private fun currentTimeSeconds(): Float {
        return (System.currentTimeMillis().toDouble() / 1000.0 % 1000.0).toFloat()
    }

    private fun ByteBuffer.putMatrix4f(offset: Int, matrix: Matrix4f) {
        putFloat(offset, matrix.m00())
        putFloat(offset + 4, matrix.m01())
        putFloat(offset + 8, matrix.m02())
        putFloat(offset + 12, matrix.m03())
        putFloat(offset + 16, matrix.m10())
        putFloat(offset + 20, matrix.m11())
        putFloat(offset + 24, matrix.m12())
        putFloat(offset + 28, matrix.m13())
        putFloat(offset + 32, matrix.m20())
        putFloat(offset + 36, matrix.m21())
        putFloat(offset + 40, matrix.m22())
        putFloat(offset + 44, matrix.m23())
        putFloat(offset + 48, matrix.m30())
        putFloat(offset + 52, matrix.m31())
        putFloat(offset + 56, matrix.m32())
        putFloat(offset + 60, matrix.m33())
    }

    private fun ByteBuffer.putVec4(index: Int, x: Float, y: Float, z: Float, w: Float) {
        val offset = index * 16
        putFloat(offset, x)
        putFloat(offset + 4, y)
        putFloat(offset + 8, z)
        putFloat(offset + 12, w)
    }
}
