/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025-2026 heckerpowered
 */

package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.util.ARGB
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

class ManaBar {
    companion object {
        private const val LEFT = 50.0
        private const val RIGHT_PADDING = 50.0
        private const val TOP = 10.0
        private const val BOTTOM = 25.0
        private const val COST_BAR_HEIGHT = 3.0

        private const val MANA_ALPHA = 64
        private const val USAGE_ALPHA = 128
        private const val COST_ALPHA = 128
    }

    private val easingFunction = ElasticEase()
    private var visibility = false

    private val shownAnimation = SimpleDoubleAnimation()
    private val opacityAnimation = SimpleDoubleAnimation()

    val maxMana = SimpleDoubleAnimation()
    val mana = SimpleDoubleAnimation()
    var manaUsage = SimpleDoubleAnimation()
    val manaCost = SimpleDoubleAnimation()

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

    private fun renderManaBar(drawContext: GuiGraphicsExtractor) {
        if (!manaUsage.isAnimating) {
            mana.value -= manaUsage.value
            manaUsage.value = .0
        }

        val windowWidth = drawContext.guiWidth()
        val fullWidth = windowWidth - LEFT - RIGHT_PADDING
        val opacity = opacityAnimation.animatedValue.coerceIn(.0, 1.0)

        val left = LEFT
        val top = TOP + shownAnimation.animatedValue
        val bottom = BOTTOM + shownAnimation.animatedValue

        val right = lerp(
            manaPercentage(),
            LEFT,
            windowWidth - RIGHT_PADDING,
        )

        drawContext.fill(
            left.roundToInt(),
            top.roundToInt(),
            right.roundToInt(),
            bottom.roundToInt(),
            color(opacity, MANA_ALPHA, 25, 128, 255),
        )

        val usagePercentage = manaUsage.animatedValue / maxMana.animatedValue
        val usageLeft = (right - usagePercentage * fullWidth).coerceAtLeast(LEFT)

        drawContext.fill(
            usageLeft.roundToInt(),
            top.roundToInt(),
            right.roundToInt(),
            bottom.roundToInt(),
            color(opacity, USAGE_ALPHA, 255, 25, 25),
        )

        val costPercentage = manaCost.animatedValue / maxMana.animatedValue
        val costLeft = (usageLeft - costPercentage * fullWidth).coerceAtLeast(LEFT)

        drawContext.fill(
            costLeft.roundToInt(),
            bottom.roundToInt(),
            usageLeft.roundToInt(),
            (bottom + COST_BAR_HEIGHT).roundToInt(),
            color(opacity, COST_ALPHA, 128, 25, 25),
        )
    }

    private fun renderManaText(drawContext: GuiGraphicsExtractor) {
        val mana = mana.animatedValue
        val manaUsage = this.manaUsage.animatedValue
        val currentMana = ((mana * 10).toLong() - (manaUsage * 10).toLong()) / 10.0
        val maxMana = (maxMana.animatedValue * 10).toLong() / 10.0

        val text = "${MatrixLanguage.mana.string} - ${formatDecimal(currentMana)}/${formatDecimal(maxMana)}"

        drawContext.text(
            Minecraft.getInstance().font,
            text,
            55,
            (12.5 + shownAnimation.animatedValue).roundToInt(),
            color(1.0, 255, 255, 255, 255),
        )
    }

    val animationRemaining: Boolean
        get() = manaUsage.isAnimating || mana.isAnimating

    fun render(drawContext: GuiGraphicsExtractor) {
        if (!visibility) {
            opacityAnimation.value = .0
            shownAnimation.value = -50.0
        }

        renderManaBar(drawContext)
        renderManaText(drawContext)
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        this.visibility = visibility
        if (visibility) {
            opacityAnimation.value = 1.0
            shownAnimation.value = .0
        }
    }

    fun forceHide() {
        visibility = false

        opacityAnimation.value = .0
        opacityAnimation.animatedValue = .0

        shownAnimation.value = -50.0
        shownAnimation.animatedValue = -50.0
    }

    fun onRemoteManaUpdate() {
    }

    private fun color(opacity: Double, alpha: Int, red: Int, green: Int, blue: Int): Int {
        return ARGB.color(
            (alpha * opacity).roundToInt().coerceIn(0, 255),
            red.coerceIn(0, 255),
            green.coerceIn(0, 255),
            blue.coerceIn(0, 255),
        )
    }

    private fun formatDecimal(value: Double, scale: Int = 1): String {
        if (!value.isFinite()) {
            return "∞"
        }

        return BigDecimal.valueOf(value)
            .setScale(scale, RoundingMode.HALF_UP)
            .toPlainString()
    }

    private fun lerp(delta: Double, from: Double, to: Double): Double {
        return from + (to - from) * delta
    }
}