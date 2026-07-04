/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import com.mojang.blaze3d.pipeline.BlendFunction
import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.shader.hud.ProgressRingRenderer
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.effect.ModMobEffects
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeMaxCharge
import heckerpowered.matrix.common.item.ModComponents.borrowedTimeState
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.util.ARGB
import org.joml.Vector2f
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

    fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        // Drop rings a skipped capture left behind (e.g. vanilla menu blur active last frame).
        pendingRings.clear()
        renderWitherArmor(drawContext, tickCounter)
        renderPhaseWalk(drawContext, tickCounter)
    }

    private class PendingRing(
        val color: Vector4f,
        val progress: Float,
        val center: Vector2f,
        val radius: Float,
        val thickness: Float,
        val aspectRatio: Float,
    )

    private val pendingRings = mutableListOf<PendingRing>()

    /**
     * Draws the rings queued during extraction into [MatrixHud.hudFramebuffer]. Called by
     * MatrixHud.onHudCaptureBegin right after the framebuffer is cleared and BEFORE the HUD
     * stratum renders into it — this runs at GUI draw time (the extraction-time drawTo used to
     * be wiped by that clear), and keeps the 1.21 order of ring below, text on top.
     */
    fun flushPendingRings() {
        for (ring in pendingRings) {
            ProgressRingRenderer.color = ring.color
            ProgressRingRenderer.progress = ring.progress
            ProgressRingRenderer.center = ring.center
            ProgressRingRenderer.radius = ring.radius
            ProgressRingRenderer.thickness = ring.thickness
            ProgressRingRenderer.aspectRatio = ring.aspectRatio
            ProgressRingRenderer.progressRingShader.drawTo(MatrixHud.hudFramebuffer, BlendFunction.TRANSLUCENT)
        }
        pendingRings.clear()
    }

    private val phaseWalkProgress = SimpleDoubleAnimation(initValue = .0)
    private val phaseWalkOpacity = SimpleDoubleAnimation(initValue = .0)
    private val phaseWalkYOffset = SimpleDoubleAnimation()

    private fun renderPhaseWalk(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        val chestplate = player.getItemBySlot(EquipmentSlot.CHEST)
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

    private fun renderWitherArmor(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        val statusEffect = player.getEffect(ModMobEffects.WitherArmorCharged)
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

    private fun renderProgressRing(drawContext: GuiGraphicsExtractor, progress: Float, progressString: String, description: String, opacity: Float, xOffset: Float = 0F, yOffset: Float = 0F) {
        MatrixHud.renderHud = true
        MatrixHud.useBloom = true

        val scaledWidth = drawContext.guiWidth()
        val scaledHeight = drawContext.guiHeight()

        val progressRingX = scaledWidth.toFloat()
        val progressRingY = scaledHeight / 2F

        val minX = progressRingX - PROGRESS_RING_SIZE - X_PADDING + xOffset
        val maxX = progressRingX - X_PADDING + xOffset

        val minY = progressRingY + PROGRESS_RING_SIZE / 2 + yOffset
        val maxY = progressRingY - PROGRESS_RING_SIZE / 2 + yOffset

        // 26.2: the ring was a POSITION_TEXTURE quad drawn through progressRingShader (the
        // quad's texcoords spanned the ring). progressRingShader is now a fullscreen
        // BlitProgram, so the quad placement moves into the shader's center/radius/thickness
        // uniforms (fragTexCoord space, y-up), and the pass targets MatrixHud.hudFramebuffer --
        // the framebuffer that was implicitly bound here in 1.21 -- so the ring still feeds the
        // blur/bloom composite (the 1.5x HDR color is preserved for the bloom pass). This runs
        // during GUI EXTRACTION, but hudFramebuffer is cleared at GUI draw time right before
        // the stratum renders, so the pass itself is queued and flushed post-clear
        // (see flushPendingRings).
        val radius = (PROGRESS_RING_SIZE / 2F) / scaledHeight
        pendingRings.add(
            PendingRing(
                color = Vector4f(1.5F, 1.5F, 1.5F, opacity),
                progress = progress,
                center = Vector2f(
                    ((minX + maxX) / 2F) / scaledWidth,
                    1F - ((minY + maxY) / 2F) / scaledHeight
                ),
                radius = radius,
                thickness = radius * 0.2F,
                aspectRatio = scaledWidth.toFloat() / scaledHeight,
            )
        )

        val width = minecraft.font.width(progressString)
        val textAlpha = (opacity * 255).toInt()
        if (textAlpha <= 3) {
            return
        }
        drawContext.text(
            minecraft.font,
            progressString,
            ((minX + maxX) / 2).toInt() - width / 2,
            ((minY + maxY) / 2).toInt() - minecraft.font.lineHeight / 2,
            ARGB.color(textAlpha, 255, 255, 255),
            true
        )

        val descWidth = minecraft.font.width(description)
        drawContext.text(
            minecraft.font,
            description,
            ((minX + maxX) / 2).toInt() - descWidth / 2,
            minY.toInt() + minecraft.font.lineHeight,
            ARGB.color(textAlpha, 255, 255, 255),
            true
        )
    }
}
