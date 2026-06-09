/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package net.minecraft.client.renderer.rendertype

import com.mojang.blaze3d.pipeline.RenderPipeline

object MatrixRenderTypes {
    private var pointSprite: RenderType? = null
    private var pointSpritePipeline: RenderPipeline? = null

    fun matrixPointSprite(pipeline: RenderPipeline): RenderType {
        val current = pointSprite
        if (current != null && pointSpritePipeline === pipeline) {
            return current
        }

        pointSpritePipeline = pipeline
        return RenderType.create(
            "matrix_point_sprite",
            RenderSetup.builder(pipeline).createRenderSetup(),
        ).also {
            pointSprite = it
        }
    }
}
