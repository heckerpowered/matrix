/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.foundation.ui.animation.core.AnimationScope
import heckerpowered.foundation.ui.animation.tween.TweenSpec
import heckerpowered.foundation.ui.color.Argb8
import heckerpowered.matrix.client.easingFunction
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.math.Vec3d
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

object DamageNumberHud {
    private val scope = AnimationScope()

    private class DamageNumber(val text: String, val color: Argb8, val position: Vec3d, scope: AnimationScope, val uid: Long) {
        var size by scope.doubleAnimation(20.0)
        var yOffset by scope.doubleAnimation(.0)
        var opacity by scope.doubleAnimation(.0)

        var isFading = false
    }

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

    private fun formatDamage(value: Float): String {
        if (value.isInfinite()) return "9999"

        if (value >= 1.0) {
            return value.roundToLong().toString()
        }

        val truncated = (value * 10.0).toInt() / 10.0
        return truncated.toString()
    }

    fun addDamageNumber(damage: Float, color: Argb8, position: Vec3d) {
        val formattedDamage = formatDamage(damage)
        addDamageNumber(formattedDamage, color, position)
    }

    private var createdThisSecond = 0
    private var lastSecondMarkNanos = System.nanoTime()

    fun addDamageNumber(text: String, color: Argb8, position: Vec3d) {
        createdThisSecond++

        val damageNumber = DamageNumber(text, color, position, scope, ++counter)
        scope.withAnimation(TweenSpec(300.milliseconds, easingFunction)) {
            damageNumber.size = 4.0
        }
        scope.withAnimation(TweenSpec(900.milliseconds, easingFunction)) {
            damageNumber.yOffset = .5
        }
        scope.withAnimation(TweenSpec(300.milliseconds, easingFunction)) {
            damageNumber.opacity = 255.0
        }
        damageNumbers.add(damageNumber)
    }

    private fun renderDamageNumber(damageNumber: DamageNumber, drawContext: DrawContext, tickCounter: RenderTickCounter) {
        if (damageNumber.opacity <= 4.0) return
        if (!damageNumber.isFading && damageNumber.opacity >= 255.0) {
            damageNumber.isFading = true
            scope.withAnimation(TweenSpec(300.milliseconds, easingFunction, 0.milliseconds)) {
                damageNumber.opacity = .0
            }
        }

        val base = damageNumber.position
        val screenPosition = worldToScreen(
            Vec3d(base.x, base.y + damageNumber.yOffset, base.z)
        ) ?: return

        val text = damageNumber.text
        val textRenderer = minecraft.textRenderer

        val width = textRenderer.getWidth(text)
        val height = textRenderer.fontHeight

        val centerX = screenPosition.x + width / 2
        val centerY = screenPosition.y + height / 2

        val matrices = drawContext.matrices
        matrices.push()
        matrices.translate(centerX, centerY, 0.0)
        matrices.scale(
            damageNumber.size.toFloat(),
            damageNumber.size.toFloat(),
            1.0f
        )
        matrices.translate(-centerX, -centerY, 0.0)

        val argb = damageNumber.color
            .withAlpha(damageNumber.opacity.toInt())
            .packed

        textRenderer.draw(
            text,
            screenPosition.x.toFloat(),
            screenPosition.y.toFloat(),
            argb,
            true,
            matrices.peek().positionMatrix,
            drawContext.vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0,
            15728880
        )

        matrices.pop()
    }

    fun onHudRender(drawContext: DrawContext, tickCounter: RenderTickCounter) {
        for (damageNumber in damageNumbers) {
            renderDamageNumber(damageNumber, drawContext, tickCounter)
        }

        for (i in damageNumbers.lastIndex downTo 0) {
            if (damageNumbers[i].opacity <= 0.0 && damageNumbers[i].isFading) {
                damageNumbers.removeAt(i)
            }
        }
    }
}