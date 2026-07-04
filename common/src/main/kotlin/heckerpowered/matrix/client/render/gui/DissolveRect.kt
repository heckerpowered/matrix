/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.gui

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexConsumer
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState

/**
 * 26.2 GUI-pipeline port of the 1.21 mesh-attached dissolve program (DissolveShader:
 * position_texture_color.vsh + noise_mask.fsh): a screen-space quad burned in/out by the
 * perlin-noise mask, with the emissive border glow. The per-draw uniforms (dissolveFactor,
 * resolution ratio, time) travel packed into the Normal attribute (gui/dissolve_rect.vsh
 * unpacks them), because vanilla GUI elements have no per-element uniform slot.
 */
class DissolveRectRenderState(
    private val x0: Float,
    private val y0: Float,
    private val x1: Float,
    private val y1: Float,
    private val color: Int,
    dissolveFactor: Float,
    resolutionRatio: Float,
    timeSeconds: Float,
    private val scissor: ScreenRectangle?,
) : GuiElementRenderState {
    // Normal-channel packing (shader unpacks with * 0.5 + 0.5, then the fragment stage
    // rescales): factor is already 0..1; the height/width ratio is stored /4 (supports
    // ratios up to 4); time wraps over the shader's 10-second drift window.
    private val packedFactor = (dissolveFactor.coerceIn(0F, 1F)) * 2F - 1F
    private val packedRatio = (resolutionRatio / 4F).coerceIn(0F, 1F) * 2F - 1F
    private val packedTime = (timeSeconds % 10F / 10F) * 2F - 1F

    override fun buildVertices(consumer: VertexConsumer) {
        // Baseline vertex order/texcoords (renderRightPart): x flipped so the burn sweep
        // travels the same direction as 1.21.
        consumer.addVertex(x1, y0, 0F).setUv(0F, 0F).setColor(color).setNormal(packedFactor, packedRatio, packedTime)
        consumer.addVertex(x0, y0, 0F).setUv(1F, 0F).setColor(color).setNormal(packedFactor, packedRatio, packedTime)
        consumer.addVertex(x0, y1, 0F).setUv(1F, 1F).setColor(color).setNormal(packedFactor, packedRatio, packedTime)
        consumer.addVertex(x1, y1, 0F).setUv(0F, 1F).setColor(color).setNormal(packedFactor, packedRatio, packedTime)
    }

    override fun pipeline(): RenderPipeline = DissolveRect.pipeline

    override fun textureSetup(): TextureSetup = DissolveRect.noiseTextureSetup()

    override fun scissorArea(): ScreenRectangle? = scissor

    override fun bounds(): ScreenRectangle = ScreenRectangle(
        x0.toInt(), y0.toInt(), (x1 - x0).toInt().coerceAtLeast(1), (y1 - y0).toInt().coerceAtLeast(1)
    )
}

object DissolveRect {
    private val noiseTextureId = Matrix.identifier("textures/noise/perlin_noise.png")

    val pipeline: RenderPipeline = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Matrix.identifier("pipeline/gui_dissolve_rect"))
        .withVertexShader(Matrix.identifier("gui/dissolve_rect"))
        .withFragmentShader(Matrix.identifier("gui/dissolve_rect"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)
        .build()

    fun noiseTextureSetup(): TextureSetup {
        // The noise drift samples beyond [0,1], so the sampler must repeat (the 1.21 GL
        // texture defaulted to GL_REPEAT).
        val view = minecraft.textureManager.getTexture(noiseTextureId).textureView
        return TextureSetup.singleTexture(view, RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR))
    }

    /**
     * Mod pipelines are built after the reload that compiles the vanilla-registered ones, so
     * compile explicitly against the ShaderManager sources (cached by the device afterwards).
     */
    fun precompile() {
        RenderSystem.getDevice().precompilePipeline(pipeline) { id, type ->
            minecraft.shaderManager.getShader(id, type)
        }
    }
}
