/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import heckerpowered.matrix.Matrix
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f
import org.joml.Matrix3x2fc
import kotlin.math.roundToInt

object MatrixGuiPipelines {
    private var dissolveRectPipeline: RenderPipeline? = null
    @Volatile
    private var precompileRequested = false

    fun dissolveRectPipeline(): RenderPipeline {
        return dissolveRectPipeline ?: RenderPipelines.register(
            RenderPipeline.builder()
                .withLocation(Matrix.identifier("pipeline/gui_dissolve_rect"))
                .withVertexShader(Matrix.identifier("gui/dissolve_rect"))
                .withFragmentShader(Matrix.identifier("gui/dissolve_rect"))
                .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, VertexFormat.Mode.QUADS)
                .withCull(false)
                .withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
                .build()
        ).also {
            dissolveRectPipeline = it
        }
    }

    fun registerKnownGuiPipelines() {
        dissolveRectPipeline()
    }

    fun requestGuiPipelinePrecompile() {
        precompileRequested = true
    }

    fun runRequestedPrecompileIfPossible() {
        if (!precompileRequested || RenderSystem.tryGetDevice() == null || !RenderSystem.isOnRenderThread()) {
            return
        }
        precompileRequested = false
        val pipeline = dissolveRectPipeline()
        val result = runCatching {
            RenderSystem.getDevice().precompilePipeline(pipeline).isValid
        }
        result.exceptionOrNull()?.let {
            Matrix.LOGGER.warn("Matrix GUI shader pipeline failed to precompile: gui/dissolve_rect", it)
        }
        if (result.getOrDefault(false)) {
            Matrix.LOGGER.info("Matrix GUI shader pipelines precompiled: 1/1")
        } else if (result.exceptionOrNull() == null) {
            Matrix.LOGGER.warn("Matrix GUI shader pipeline is invalid after precompile: gui/dissolve_rect")
        }
    }

    fun drawDissolveRect(
        drawContext: GuiGraphicsExtractor,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        color: Int,
        dissolveFactor: Float = 1.0F,
    ) {
        val textureSetup = dissolveTextureSetup() ?: run {
            drawContext.fill(left, top, right, bottom, color)
            return
        }
        drawContext.guiRenderState.addGuiElement(
            DissolveRectRenderState(
                dissolveRectPipeline(),
                textureSetup,
                Matrix3x2f(drawContext.pose()),
                left,
                top,
                right,
                bottom,
                color,
                dissolveFactor.coerceIn(.0F, 1.0F),
                (right - left).toFloat().coerceAtLeast(1.0F),
                (bottom - top).toFloat().coerceAtLeast(1.0F),
                drawContext.scissorStack.peek(),
            )
        )
    }

    private fun dissolveTextureSetup(): TextureSetup? {
        val noiseTexture = MatrixShaderTextures.perlinNoiseTextureView() ?: return null
        val sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
        return TextureSetup.singleTexture(noiseTexture, sampler)
    }

    private data class DissolveRectRenderState(
        private val pipeline: RenderPipeline,
        private val textureSetup: TextureSetup,
        private val pose: Matrix3x2fc,
        private val x0: Int,
        private val y0: Int,
        private val x1: Int,
        private val y1: Int,
        private val color: Int,
        private val dissolveFactor: Float,
        private val width: Float,
        private val height: Float,
        private val scissorArea: ScreenRectangle?,
    ) : GuiElementRenderState {
        private val bounds = ScreenRectangle(x0, y0, x1 - x0, y1 - y0)
            .transformMaxBounds(pose)
            .let { transformed ->
                scissorArea?.let(transformed::intersection) ?: transformed
            }

        override fun pipeline() = pipeline

        override fun textureSetup() = textureSetup

        override fun scissorArea() = scissorArea

        override fun bounds() = bounds

        override fun buildVertices(vertexConsumer: VertexConsumer) {
            val aspect = (height / width).coerceIn(.0F, 1.0F)
            val time = ((System.nanoTime() / 1_000_000_000.0) % 10.0 / 10.0).toFloat()
            vertex(vertexConsumer, x0.toFloat(), y0.toFloat(), .0F, .0F, dissolveFactor, aspect, time)
            vertex(vertexConsumer, x0.toFloat(), y1.toFloat(), .0F, 1.0F, dissolveFactor, aspect, time)
            vertex(vertexConsumer, x1.toFloat(), y1.toFloat(), 1.0F, 1.0F, dissolveFactor, aspect, time)
            vertex(vertexConsumer, x1.toFloat(), y0.toFloat(), 1.0F, .0F, dissolveFactor, aspect, time)
        }

        private fun vertex(
            vertexConsumer: VertexConsumer,
            x: Float,
            y: Float,
            u: Float,
            v: Float,
            dissolveFactor: Float,
            aspect: Float,
            time: Float,
        ) {
            vertexConsumer
                .addVertexWith2DPose(pose, x, y)
                .setUv(u, v)
                .setColor(color)
                .setNormal(
                    encodeSignedNormal(dissolveFactor),
                    encodeSignedNormal(aspect),
                    encodeSignedNormal(time),
                )
        }

        private fun encodeSignedNormal(value: Float): Float {
            return ((value.coerceIn(.0F, 1.0F) * 2.0F - 1.0F) * 127.0F).roundToInt() / 127.0F
        }
    }
}
