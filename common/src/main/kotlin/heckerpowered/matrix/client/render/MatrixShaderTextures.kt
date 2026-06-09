/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.textures.GpuTextureView
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.resources.Identifier

object MatrixShaderTextures {
    private val perlinNoiseId = Matrix.identifier("textures/noise/perlin_noise.png")
    private val vortexNoiseId = Matrix.identifier("textures/noise/vortex_noise.png")

    fun perlinNoiseTextureView(): GpuTextureView? {
        return textureView(perlinNoiseId)
    }

    fun vortexNoiseTextureView(): GpuTextureView? {
        return textureView(vortexNoiseId) ?: perlinNoiseTextureView()
    }

    private fun textureView(identifier: Identifier): GpuTextureView? {
        return runCatching {
            minecraft.textureManager.getTexture(identifier).textureView
        }.getOrNull()
    }
}
