/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.magic.channel.CasterContext
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.common.magic.rule.resource.CastingResourcePipeline
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ManaBar {
    companion object {
        val manaBarColor = Color(25, 128, 255, 64)
        val usageManaColor = Color(255, 25, 25, 128)
        val costManaColor = Color(128, 25, 25, 128)
    }

    private val easingFunction = ElasticEase()
    private var visibility = false

    private val shownAnimation = SimpleDoubleAnimation()
    private val opacityAnimation = SimpleDoubleAnimation()

    val maxMana = SimpleDoubleAnimation()
    val mana = SimpleDoubleAnimation()
    var manaUsage = SimpleDoubleAnimation()
    val manaCost = SimpleDoubleAnimation()

    private val castingResources = SimpleDoubleAnimation()

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0
    }

    private fun manaPercentage(): Double {
        if (mana.value.isInfinite()) {
            return 1.0
        }
        return mana.animatedValue / maxMana.animatedValue
    }

    private val actualManaPercentage: Double
        get() = if (mana.value.isInfinite()) {
            1.0
        } else {
            (mana.animatedValue - manaUsage.animatedValue) / maxMana.animatedValue
        }

    private val dissolveShader = DissolveShader()

    private fun renderManaBar(drawContext: DrawContext, renderer: LegacyMatrixUIRenderer) {
        if (!manaUsage.isAnimating) {
            mana.value -= manaUsage.value
            manaUsage.value = .0
        }
        val minPoint = Point(
            50.0,
            10.0 + shownAnimation.animatedValue
        )
        val maxPoint = Point(
            MathHelper.lerp(manaPercentage(), 50.0, renderer.scaledWindowWidth - 50.0),
            25.0 + shownAnimation.animatedValue
        )

        /**
        val width = maxPoint.x - minPoint.x
        val height = maxPoint.y - minPoint.y
        dissolveShader.dissolveFactor = 1.0F - actualManaPercentage.toFloat() / 2.0F
        dissolveShader.resolutionX = width.toFloat() / 10.0F
        dissolveShader.resolutionY = height.toFloat() / 10.0F
        dissolveShader.emissiveColor = Vector4f(0.1F, 0.5F, 1F, 1.0F)
        dissolveShader.plainDissolveProgram.enableShader()

        val backgroundColor = ColorHelper.Argb.getArgb(255, 0, 0, 0)
        val builder = Tessellator.getInstance()
        val buffer = builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)

        buffer.vertex(minPoint.x.toFloat(), maxPoint.y.toFloat(), 0F).texture(1F, 1F).color(backgroundColor)
        buffer.vertex((renderer.scaledWindowWidth - 50.0).toFloat(), maxPoint.y.toFloat(), 0F).texture(0F, 1F).color(backgroundColor)
        buffer.vertex((renderer.scaledWindowWidth - 50.0).toFloat(), minPoint.y.toFloat(), 0F).texture(0F, 0F).color(backgroundColor)
        buffer.vertex(minPoint.x.toFloat(), minPoint.y.toFloat(), 0F).texture(1F, 0F).color(backgroundColor)

        val window = MinecraftClient.getInstance().window
        val scaleFactor = window.scaleFactor
        val maxX = MathHelper.lerp(actualManaPercentage, 50.0, renderer.scaledWindowWidth - 50.0)
        RenderSystem.enableScissor(
        (minPoint.x * scaleFactor).toInt(),
        (window.framebufferHeight - maxPoint.y * scaleFactor).toInt(),
        ((maxX - minPoint.x) * scaleFactor).toInt(),
        ((maxPoint.y - minPoint.y) * scaleFactor).toInt(),
        )
        isolate(
        BlendState(true),
        BlendFuncSeparateState()
        ) {
        BufferRenderer.draw(buffer.end())
        }
        RenderSystem.disableScissor()

        dissolveShader.disableShader()
        dissolveShader.resolutionX = 1.0F
        dissolveShader.resolutionY = 1.0F
         */
        val multiplier = 1F
        RenderSystem.setShaderColor(multiplier, multiplier, multiplier, 1.0F)
        renderer.renderRectangle(Rectangle(minPoint, maxPoint), manaBarColor)
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)

        val usagePercentage = manaUsage.animatedValue / maxMana.animatedValue
        val usageMinPoint = Point(
            (maxPoint.x - usagePercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            minPoint.y
        )

        renderer.renderRectangle(Rectangle(usageMinPoint, maxPoint), usageManaColor)

        val costPercentage = manaCost.animatedValue / maxMana.animatedValue
        val costMinPoint = Point(
            (usageMinPoint.x - costPercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            maxPoint.y
        )

        // val multiplier = 1.0F
        // RenderSystem.setShaderColor(multiplier, multiplier, multiplier, multiplier)
        renderer.renderRectangle(Rectangle(costMinPoint, Point(usageMinPoint.x, maxPoint.y + 3)), costManaColor)
        // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    }

    private fun renderManaText(renderer: LegacyMatrixUIRenderer) {
        val mana = mana.animatedValue
        val manaUsage = this.manaUsage.animatedValue
        val currentMana = ((mana * 10).toLong() - (manaUsage * 10).toLong()) / 10.0
        val maxMana = (maxMana.animatedValue * 10).toLong() / 10.0

        val context = MagicCalculationContext(CasterContext.fromEntity(player))
        castingResources.value = CastingResourcePipeline.collect(context).resources.drop(1).sumOf { it.availableAmount(context).toDouble() }

        val total = (castingResources.animatedValue * 10).toLong() / 10.0
        if (total != 0.0) {
            renderer.render(
                Text.literal("${MatrixLanguage.mana.string} ≈ $total + ${currentMana}/${maxMana}"),
                Point(55.0, 12.5 + shownAnimation.animatedValue), Color(255, 255, 255, 255),
                true
            )
        } else {
            renderer.render(
                Text.literal("${MatrixLanguage.mana.string} = ${currentMana}/${maxMana}"),
                Point(55.0, 12.5 + shownAnimation.animatedValue), Color(255, 255, 255, 255),
                true
            )
        }
    }

    val animationRemaining: Boolean
        get() = manaUsage.isAnimating || mana.isAnimating

    fun render(drawContext: DrawContext, renderer: LegacyMatrixUIRenderer) {
        if (!visibility) {
            opacityAnimation.value = .0
            shownAnimation.value = -50.0
        }

        manaBarColor.alpha = (opacityAnimation.animatedValue * 127.5).toInt()
        usageManaColor.alpha = (opacityAnimation.animatedValue * 127.5).toInt()
        costManaColor.alpha = (opacityAnimation.animatedValue * 127.5).toInt()

        renderManaBar(drawContext, renderer)
        renderManaText(renderer)
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        this.visibility = visibility
        if (visibility) {
            opacityAnimation.value = 1.0
            shownAnimation.value = .0
        }
    }

    fun onRemoteManaUpdate() {
    }
}