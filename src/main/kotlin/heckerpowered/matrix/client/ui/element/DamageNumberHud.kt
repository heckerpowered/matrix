/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import java.time.Duration
import kotlin.math.floor

object DamageNumberHud {
    private data class DamageNumber(val damage: Float, val rgbColor: Vector3f, val position: Vec3d, val size: SimpleDoubleAnimation, var opacity: SimpleDoubleAnimation, val uid: Long)

    private val damageNumbers = mutableListOf<DamageNumber>()
    private var counter = 0L

    fun nextInstantKillMessage(uid: Long): String {
        return when (uid % 5) {
            0L -> "代码是这样写的"
            1L -> "跟我的保险说去吧"
            2L -> "我还以为是减速带呢"
            3L -> "我们都在用力的活着"
            4L -> "我都用力了你怎么还活着"
            else -> "114514"
        }
    }

    fun onInitialize() {
        HudRenderCallback.EVENT.register(this::onHudRender)
    }

    fun addDamageNumber(damage: Float, color: Vector3f, position: Vec3d) {
        val damageNumber = DamageNumber(damage, color, position, SimpleDoubleAnimation(), SimpleDoubleAnimation(), counter++)

        damageNumber.size.from = 20.0
        damageNumber.size.to = 2.0
        damageNumber.size.duration = Duration.ofMillis(300)
        damageNumber.size.start()

        damageNumber.opacity.value = 255.0

        damageNumbers.add(damageNumber)
    }

    private fun renderDamageNumber(damageNumber: DamageNumber, drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (damageNumber.opacity.animatedValue == 255.0 && damageNumber.opacity.from != 255.0) {
            damageNumber.opacity = SimpleDoubleAnimation().apply {
                from = 255.0
                to = .0
                startTime = Duration.ofMillis(300)
                start()
            }
        }
        if (damageNumber.opacity.animatedValue < 4) {
            return
        }
        val matrixStack = drawContext.matrices
        val damageNumberPosition = worldToScreen(damageNumber.position) ?: return

        val textRenderer = minecraft.textRenderer
        val damageText = if (damageNumber.damage.isInfinite()) {
            "9999"
        } else if (damageNumber.damage >= 1) {
            "${floor(damageNumber.damage).toULong()}"
        } else {
            "${(damageNumber.damage * 10.0).toULong().toDouble() / 10.0}"
        }
        val damageTextWidth = textRenderer.getWidth(damageText)
        val damageTextHeight = textRenderer.fontHeight

        val centerX = damageNumberPosition.x + damageTextWidth / 2
        val centerY = damageNumberPosition.y + damageTextHeight / 2

        matrixStack.push()
        matrixStack.translate(centerX, centerY, 0.0)
        matrixStack.scale(
            damageNumber.size.animatedValue.toFloat(),
            damageNumber.size.animatedValue.toFloat(),
            1.0F
        )
        matrixStack.translate(-centerX, -centerY, 0.0)

        val transformationMatrix = matrixStack.peek().positionMatrix
        textRenderer.draw(
            damageText,
            damageNumberPosition.x.toFloat(), damageNumberPosition.y.toFloat(),
            ColorHelper.Argb.getArgb(
                damageNumber.opacity.animatedValue.toInt(),
                damageNumber.rgbColor.x.toInt(),
                damageNumber.rgbColor.y.toInt(),
                damageNumber.rgbColor.z.toInt()
            ),
            true,
            transformationMatrix,
            drawContext.vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0,
            15728880
        )
        matrixStack.pop()
        drawContext.draw()
    }

    fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        for (damageNumber in damageNumbers) {
            renderDamageNumber(damageNumber, drawContext, tickCounter)
        }

        damageNumbers.removeIf {
            it.opacity.animatedValue == .0
        }
    }
}