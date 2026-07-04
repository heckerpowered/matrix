/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.foundation.ui.animation.core.AnimationScope
import heckerpowered.foundation.ui.animation.tween.TweenSpec
import heckerpowered.foundation.ui.color.Argb8
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.easingFunction
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.core.worldToScreen
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.DeltaTracker
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

object DamageNumberHud {
    private val scope = AnimationScope()

    private class DamageNumber(val text: String, val color: Argb8, val position: Vec3, scope: AnimationScope, val uid: Long) {
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
        // 26.2: HudRenderCallback was replaced by HudElementRegistry; still invoked once per rendered frame.
        HudElementRegistry.addLast(Matrix.identifier("damage_numbers")) { drawContext, tickCounter ->
            onHudRender(drawContext, tickCounter)
        }
    }

    private fun formatDamage(value: Float): String {
        if (value.isInfinite()) return "9999"

        if (value >= 1.0) {
            return value.roundToLong().toString()
        }

        val truncated = (value * 10.0).toInt() / 10.0
        return truncated.toString()
    }

    fun addDamageNumber(damage: Float, color: Argb8, position: Vec3) {
        val formattedDamage = formatDamage(damage)
        addDamageNumber(formattedDamage, color, position)
    }

    private var createdThisSecond = 0
    private var lastSecondMarkNanos = System.nanoTime()

    fun addDamageNumber(text: String, color: Argb8, position: Vec3) {
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

    private fun renderDamageNumber(damageNumber: DamageNumber, drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        if (damageNumber.opacity <= 4.0) return
        if (!damageNumber.isFading && damageNumber.opacity >= 255.0) {
            damageNumber.isFading = true
            scope.withAnimation(TweenSpec(300.milliseconds, easingFunction, 0.milliseconds)) {
                damageNumber.opacity = .0
            }
        }

        val base = damageNumber.position
        val screenPosition = worldToScreen(
            Vec3(base.x, base.y + damageNumber.yOffset, base.z)
        ) ?: return

        val text = damageNumber.text
        val font = minecraft.font

        val width = font.width(text)
        val height = font.lineHeight

        val centerX = screenPosition.x + width / 2
        val centerY = screenPosition.y + height / 2

        val pose = drawContext.pose()
        pose.pushMatrix()
        pose.translate(centerX.toFloat(), centerY.toFloat())
        pose.scale(
            damageNumber.size.toFloat(),
            damageNumber.size.toFloat()
        )
        pose.translate(-centerX.toFloat(), -centerY.toFloat())

        val argb = damageNumber.color
            .withAlpha(damageNumber.opacity.toInt())
            .packed

        // 26.2: text()'s no-lightmap 2D path already renders on top like the old SEE_THROUGH
        // layer (full-bright, no depth test) — light-value/layer params no longer exposed.
        drawContext.text(
            font,
            text,
            screenPosition.x.roundToInt(),
            screenPosition.y.roundToInt(),
            argb,
            true
        )

        pose.popMatrix()
    }

    fun onHudRender(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
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