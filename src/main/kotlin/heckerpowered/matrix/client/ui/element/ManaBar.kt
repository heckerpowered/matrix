package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

class ManaBar {
    companion object {
        val manaBarColor = Color(0, 128, 255, 64)
        val usageManaColor = Color(255, 0, 0, 128)
        val costManaColor = Color(128, 0, 0, 128)
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

        renderer.renderRectangle(Rectangle(minPoint, maxPoint), manaBarColor)

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
        renderer.renderRectangle(Rectangle(costMinPoint, Point(usageMinPoint.x, maxPoint.y + 3)), costManaColor)
    }

    private fun renderManaText(renderer: LegacyMatrixUIRenderer) {
        val mana = mana.animatedValue
        val manaUsage = this.manaUsage.animatedValue
        val currentMana = ((mana * 10).toLong() - (manaUsage * 10).toLong()) / 10.0
        val maxMana = (maxMana.animatedValue * 10).toLong() / 10.0
        renderer.render(
            Text.literal("${MatrixLanguage.mana.string} - ${currentMana}/${maxMana}"),
            Point(55.0, 12.5 + shownAnimation.animatedValue), Color(255, 255, 255, 255),
            true
        )
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