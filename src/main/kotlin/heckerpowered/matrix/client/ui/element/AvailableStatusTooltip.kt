/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.easingFunction
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.magic.MagicAvailableStatus
import heckerpowered.matrix.common.magic.description
import net.minecraft.client.gui.DrawContext
import java.time.Duration
import kotlin.math.min

object AvailableStatusTooltip {
    private val opacityClock = AnimationClock(Duration.ofMillis(300), 128.0, 0.0)
    private val shownAnimationClock = AnimationClock(Duration.ofMillis(300), -50.0, .0)
    private val shownAnimation = DoubleAnimation(shownAnimationClock, easingFunction)
    private val opacityAnimation = DoubleAnimation(opacityClock, easingFunction)

    private var currentStatus = MagicAvailableStatus.AVAILABLE
    private var displayStatus = MagicAvailableStatus.AVAILABLE
    private val statusChangeAnimation = SimpleDoubleAnimation(initValue = 1.0, duration = Duration.ofMillis(150))

    fun show() {
        opacityAnimation.currentValue = 1.0
        shownAnimation.currentValue = 0.0
    }

    fun hide() {
        opacityAnimation.currentValue = 0.0
        shownAnimation.currentValue = -50.0
    }

    fun render(drawContext: DrawContext, renderer: LegacyMatrixUIRenderer, status: MagicAvailableStatus) {
        val minPoint = Point(
            renderer.scaledWindowWidth / 2 - 125.0,
            30.0 + shownAnimation.animatedValue
        )
        val maxPoint = Point(
            renderer.scaledWindowWidth / 2 + 125.0,
            45.0 + shownAnimation.animatedValue
        )

        val backgroundColor = Color(128, 0, 0, (128 * opacityAnimation.animatedValue).toInt())
        renderer.renderRectangle(Rectangle(minPoint, maxPoint), backgroundColor)

        if (currentStatus != status && status != MagicAvailableStatus.AVAILABLE) {
            currentStatus = status
            statusChangeAnimation.value = .0
        }
        if (statusChangeAnimation.animatedValue == .0) {
            statusChangeAnimation.value = 1.0
            displayStatus = currentStatus
        }

        val opacity = (min(opacityAnimation.animatedValue, statusChangeAnimation.animatedValue) * 255).toInt()
        if (opacity <= 3) {
            return
        }

        renderer.render(
            displayStatus.description,
            Point(
                renderer.scaledWindowWidth / 2 - 125.0 + 5,
                32.5 + shownAnimation.animatedValue
            ),
            Color(255, 255, 255, opacity),
            true
        )
    }
}