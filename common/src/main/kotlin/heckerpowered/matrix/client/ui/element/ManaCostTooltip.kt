/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.MatrixHud
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.client.player
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.ui.foundation.animation.ColorAnimation
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.common.magic.core.MagicCalculationContext
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.ARGB
import java.time.Duration
import kotlin.math.abs
import kotlin.math.min

object ManaCostTooltip {
    private val shownAnimation = SimpleDoubleAnimation()
    private val opacityAnimation = SimpleDoubleAnimation()
    private val backgroundColorAnimation = ColorAnimation()
    private var visibility = false

    private var differenceChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
    private var stateChangedAnimation = SimpleDoubleAnimation(duration = Duration.ofMillis(150))
    private var lastState = false
    private var displayState = false

    private var lastDifference = 0L
    private var displayedDifference = 0L

    fun show() {
        if (!visibility) {
            return
        }
        shownAnimation.value = 70.0
        opacityAnimation.value = 1.0
    }

    fun hide() {
        shownAnimation.value = .0
        opacityAnimation.value = 0.0
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        this.visibility = visibility
        if (!visibility) {
            hide()
        }
    }

    fun render(drawContext: GuiGraphicsExtractor, tickCounter: DeltaTracker) {
        val currentMagic = MatrixHud.selectedMagic
        val target = MatrixHud.targetedEntity
        val calculationContext = MagicCalculationContext.fromEntity(player, target)
        val cost = currentMagic.getCost(calculationContext)
        val normalCost = currentMagic.getNormalCost()
        if (cost == normalCost) {
            hide()
        } else {
            show()
        }

        if (cost > normalCost) {
            backgroundColorAnimation.red.value = 0.5
            backgroundColorAnimation.green.value = .0
            backgroundColorAnimation.blue.value = .0
        } else if (cost < normalCost) {
            backgroundColorAnimation.red.value = .0
            backgroundColorAnimation.green.value = 0.5
            backgroundColorAnimation.blue.value = .0
        }
        if (opacityAnimation.animatedValue == .0) {
            return
        }

        val minPoint = Point(drawContext.guiWidth() / 2 - 125.0, drawContext.guiHeight() - shownAnimation.animatedValue)
        val maxPoint = Point(drawContext.guiWidth() / 2 + 125.0, minPoint.y + 15)

        val color = backgroundColorAnimation
        val red = (color.red.animatedValue * 255.0).toInt()
        val green = (color.green.animatedValue * 255.0).toInt()
        val blue = (color.blue.animatedValue * 255.0).toInt()
        val alpha = (opacityAnimation.animatedValue * 0.5 * 255.0).toInt()

        // 26.2: manual Tessellator/BufferRenderer quad replaced by GuiGraphicsExtractor.fill,
        // which draws the same axis-aligned rectangle through the GUI render pipeline.
        drawContext.fill(minPoint.x.toInt(), minPoint.y.toInt(), maxPoint.x.toInt(), maxPoint.y.toInt(), ARGB.color(alpha, red, green, blue))

        val textRenderer = minecraft.font
        val difference = abs(normalCost - cost)
        if (difference != lastDifference) {
            differenceChangedAnimation.value = .0
            lastDifference = difference
        }
        if (differenceChangedAnimation.animatedValue == .0) {
            differenceChangedAnimation.value = 1.0
            displayedDifference = difference
        }

        if (cost != normalCost) {
            val state = cost > normalCost
            if (state != lastState) {
                lastState = state
                stateChangedAnimation.value = .0
            }
        }
        if (stateChangedAnimation.animatedValue == .0) {
            stateChangedAnimation.value = 1.0
            displayState = lastState
        }

        val foregroundOpacity = min(differenceChangedAnimation.animatedValue, opacityAnimation.animatedValue)
        val stateForegroundOpacity = (min(stateChangedAnimation.animatedValue, opacityAnimation.animatedValue) * 255).toInt()
        val foregroundColor = Color(255, 255, 255, stateForegroundOpacity)
        val differenceForegroundColor = Color(255, 255, 255, (foregroundOpacity * 255).toInt())
        if ((opacityAnimation.animatedValue * 255).toInt() <= 3) {
            return
        }

        val yOffset = 2.5F

        // 26.2: manual textRenderer.draw(...) into a vertex consumer replaced by
        // GuiGraphicsExtractor.text, which renders through the GUI text pipeline.
        if (stateForegroundOpacity > 3) {
            if (displayState) {
                drawContext.text(textRenderer, MatrixLanguage.manaCostIncreased, (minPoint.x + 5.0).toInt(), (minPoint.y + yOffset).toInt(), foregroundColor.toInt(), false)
            } else {
                drawContext.text(textRenderer, MatrixLanguage.manaCostReduced, (minPoint.x + 5.0).toInt(), (minPoint.y + yOffset).toInt(), foregroundColor.toInt(), false)
            }
        }

        if ((foregroundOpacity * 255).toInt() > 3 && cost != normalCost) {
            val width = textRenderer.width(displayedDifference.toString())
            drawContext.text(textRenderer, displayedDifference.toString(), (maxPoint.x - 5.0).toInt() - width, (minPoint.y + yOffset).toInt(), differenceForegroundColor.toInt(), false)
        }
    }
}