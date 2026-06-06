/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.shader.hud.ProgressRingRenderer
import heckerpowered.matrix.client.render.state.BlendFuncSeparateState
import heckerpowered.matrix.client.render.state.ProgramState
import heckerpowered.matrix.client.render.state.StateIsolation
import heckerpowered.matrix.client.render.state.capabilities.BlendState
import heckerpowered.matrix.client.render.state.capabilities.CullFaceState
import heckerpowered.matrix.client.render.state.capabilities.DepthTestState
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.ModMobEffects.WITHER_ARMOR_CHARGED_EFFECT
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeMaxCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*
import net.minecraft.entity.EquipmentSlot
import net.minecraft.util.math.ColorHelper
import org.joml.Vector4f

object StatusHud {
    private const val PROGRESS_RING_SIZE = 32F
    private const val X_PADDING = 16F

    private var previousCharges = -1
    private val progress = SimpleDoubleAnimation(initValue = .0)
    private val opacityAnimation = SimpleDoubleAnimation(initValue = .0)

    fun onInitialize() {
        // HudRenderCallback.EVENT.register(this::onHudRender)
    }

    fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        renderWitherArmor(drawContext, tickCounter)
        renderPhaseWalk(drawContext, tickCounter)
    }

    private val phaseWalkProgress = SimpleDoubleAnimation(initValue = .0)
    private val phaseWalkOpacity = SimpleDoubleAnimation(initValue = .0)
    private val phaseWalkYOffset = SimpleDoubleAnimation()

    private fun renderPhaseWalk(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val chestplate = player.getEquippedStack(EquipmentSlot.CHEST)
        val isPhaseWalking = chestplate.components.getOrDefault(borrowedTimeState, false)
        val currentCharge = chestplate.components.getOrDefault(borrowedTimeCharge, 0L)
        val maxCharge = chestplate.components.getOrDefault(borrowedTimeMaxCharge, 0L)
        if (isPhaseWalking) {
            phaseWalkOpacity.value = 1.0
        } else if (currentCharge >= maxCharge) {
            phaseWalkOpacity.value = 0.0
        }

        if (phaseWalkOpacity.animatedValue == .0) {
            return
        }
        if (opacityAnimation.to == .0) {
            phaseWalkYOffset.value = .0
        } else {
            phaseWalkYOffset.value = 65.0
        }

        phaseWalkProgress.value = currentCharge.toDouble() / maxCharge.toDouble()
        renderProgressRing(
            drawContext, phaseWalkProgress.animatedValue.toFloat(),
            "${(phaseWalkProgress.animatedValue * 100).toInt()}%", "时不我待",
            phaseWalkOpacity.animatedValue.toFloat(),
            yOffset = phaseWalkYOffset.animatedValue.toFloat()
        )
    }

    private fun renderWitherArmor(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        val statusEffect = player.getStatusEffect(WITHER_ARMOR_CHARGED_EFFECT)
        if (statusEffect != null) {
            val previousValue = progress.value
            progress.value = 1.0 - statusEffect.duration / 200.0
            if (statusEffect.amplifier >= 3) {
                progress.value = 1.0
            } else if (progress.value < previousValue) {
                progress.animatedValue = progress.value
            }
            if (previousCharges != statusEffect.amplifier) {
                previousCharges = statusEffect.amplifier
                opacityAnimation.value = 1.0
            } else if (statusEffect.amplifier == 3) {
                opacityAnimation.value = .0
            }
        } else {
            opacityAnimation.value = .0
        }

        if (opacityAnimation.animatedValue == .0) {
            return
        }

        renderProgressRing(drawContext, progress.animatedValue.toFloat(), (statusEffect?.amplifier ?: 0).toString(), "凋零护甲", opacityAnimation.animatedValue.toFloat())
    }

    private fun renderProgressRing(drawContext: DrawContext, progress: Float, progressString: String, description: String, opacity: Float, xOffset: Float = 0F, yOffset: Float = 0F) {
        MatrixHud.renderHud = true
        MatrixHud.useBloom = true

        val scaledWidth = drawContext.scaledWindowWidth
        val scaledHeight = drawContext.scaledWindowHeight

        val progressRingX = scaledWidth.toFloat()
        val progressRingY = scaledHeight / 2F

        val minX = progressRingX - PROGRESS_RING_SIZE - X_PADDING + xOffset
        val maxX = progressRingX - X_PADDING + xOffset

        val minY = progressRingY + PROGRESS_RING_SIZE / 2 + yOffset
        val maxY = progressRingY - PROGRESS_RING_SIZE / 2 + yOffset

        val transformationMatrix = drawContext.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)

        buffer.vertex(transformationMatrix, minX, minY, 0F).texture(0F, 0F)
        buffer.vertex(transformationMatrix, maxX, minY, 0F).texture(1F, 0F)
        buffer.vertex(transformationMatrix, maxX, maxY, 0F).texture(1F, 1F)
        buffer.vertex(transformationMatrix, minX, maxY, 0F).texture(0F, 1F)

        ProgressRingRenderer.color = Vector4f(1.5F, 1.5F, 1.5F, opacity)
        ProgressRingRenderer.progress = progress

        StateIsolation.isolate(
            BlendState(true),
            BlendFuncSeparateState(),
            DepthTestState(false),
            CullFaceState(false),
            ProgramState(ProgressRingRenderer.progressRingShader)
        ) {
            BufferRenderer.draw(buffer.end())
        }

        val width = minecraft.textRenderer.getWidth(progressString)
        val textAlpha = (opacity * 255).toInt()
        if (textAlpha <= 3) {
            return
        }
        drawContext.drawText(
            minecraft.textRenderer,
            progressString,
            ((minX + maxX) / 2).toInt() - width / 2,
            ((minY + maxY) / 2).toInt() - minecraft.textRenderer.fontHeight / 2,
            ColorHelper.Argb.getArgb(textAlpha, 255, 255, 255),
            true
        )

        val descWidth = minecraft.textRenderer.getWidth(description)
        drawContext.drawText(
            minecraft.textRenderer,
            description,
            ((minX + maxX) / 2).toInt() - descWidth / 2,
            minY.toInt() + minecraft.textRenderer.fontHeight,
            ColorHelper.Argb.getArgb(textAlpha, 255, 255, 255),
            true
        )
    }
}