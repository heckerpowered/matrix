package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.shader.BlurRenderer
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import java.time.Duration

class ManaBar {
    companion object {
        val manaBarColor = Color(0, 128, 255, 64)
        val usageManaColor = Color(255, 0, 0, 128)
        val costManaColor = Color(128, 0, 0, 128)
    }

    private val easingFunction = ElasticEase()

    private val maxManaClock = AnimationClock(Duration.ofMillis(300), 100.0, 100.0)
    private val manaClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val manaUsageClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val manaCostClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val opacityClock = AnimationClock(Duration.ofMillis(300), 128.0, 0.0)
    private val shownAnimationClock = AnimationClock(Duration.ofMillis(300), -50.0, .0)
    private val shownAnimation = DoubleAnimation(shownAnimationClock, easingFunction)

    private var privateCurrentMana = 0.0
    private var privateManaUsage = 0.0
    private var privateManaCost = 0.0

    private var visibility = false

    val maxMana = DoubleAnimation(maxManaClock, easingFunction)
    val mana = DoubleAnimation(manaClock, easingFunction)

    var manaUsage
        get() = privateManaUsage
        set(value) {
            val newValue = value.coerceIn(0.0..mana.currentValue)
            if (newValue == privateManaUsage) {
                return
            }
            val lastManaUsage = privateManaUsage
            privateManaUsage = newValue

            manaUsageClock.from = lastManaUsage
            manaUsageClock.to = privateManaUsage
            manaUsageClock.start()
        }

    var manaCost
        get() = privateManaCost
        set(value) {
            val lastManaCost = privateManaCost
            privateManaCost = value

            manaCostClock.from = lastManaCost
            manaCostClock.to = privateManaCost
            manaCostClock.start()
        }

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0

        maxMana.currentValue = 100.0
    }

    private fun manaPercentage(): Double {
        if (mana.currentValue.isInfinite()) {
            return 1.0
        }
        return mana.animatedValue / maxMana.animatedValue
    }

    private fun renderManaBar(drawContext: DrawContext, renderer: LegacyMatrixUIRenderer) {
        val manaUsage = manaUsageClock.transform(easingFunction)
        if (manaUsage != 0.0 && manaUsage == privateManaUsage) {
            mana.currentValue -= manaUsage
            this.manaUsage = 0.0
        }
        val minPoint = Point(
            50.0,
            10.0 + shownAnimation.animatedValue
        )
        val maxPoint = Point(
            MathHelper.lerp(manaPercentage(), 50.0, renderer.scaledWindowWidth - 50.0),
            25.0 + shownAnimation.animatedValue
        )

        drawContext.enableScissor(
            minPoint.x.toInt(),
            minPoint.y.toInt(),
            maxPoint.x.toInt(),
            maxPoint.y.toInt()
        )
        BlurRenderer.blurTextureRenderShader.enableShader()
        BlurRenderer.renderQuad()
        BlurRenderer.blurTextureRenderShader.disableShader()

        drawContext.disableScissor()

        // println("mana percentage: ${manaPercentage()}, minPoint: $minPoint, maxPoint: $maxPoint")
        renderer.renderRectangle(Rectangle(minPoint, maxPoint), manaBarColor)

        val usagePercentage = manaUsageClock.transform(easingFunction) / maxMana.animatedValue
        val usageMinPoint = Point(
            (maxPoint.x - usagePercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            minPoint.y
        )
        renderer.renderRectangle(Rectangle(usageMinPoint, maxPoint), usageManaColor)

        val costPercentage = manaCostClock.transform(easingFunction) / maxMana.animatedValue
        val costMinPoint = Point(
            (usageMinPoint.x - costPercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            maxPoint.y
        )
        renderer.renderRectangle(Rectangle(costMinPoint, Point(usageMinPoint.x, maxPoint.y + 3)), costManaColor)
    }

    private fun renderManaText(renderer: LegacyMatrixUIRenderer) {
        // val manaUsage = manaUsageClock.transform(easingFunction)
        val mana = (mana.animatedValue * 10).toInt() / 10.0
        val maxMana = (maxMana.animatedValue * 10).toInt() / 10.0
        renderer.render(
            Text.literal("${MatrixLanguage.mana.string} - ${mana}/${maxMana}"),
            Point(55.0, 12.5 + shownAnimation.animatedValue), Color(255, 255, 255, 255),
            true
        )
    }

    fun render(drawContext: DrawContext, renderer: LegacyMatrixUIRenderer) {
        if (!visibility && manaClock.getValue() >= 1.0) {
            opacityClock.let {
                it.from = it.transform(easingFunction)
                it.to = 0.0
                it.start()
            }

            shownAnimationClock.let {
                it.from = shownAnimation.animatedValue
                it.to = -50.0
                it.start()
            }
        }

        manaBarColor.alpha = opacityClock.transform(easingFunction).toInt()

        renderManaBar(drawContext, renderer)
        renderManaText(renderer)
    }

    fun onHudVisibilityChanged(visibility: Boolean) {
        this.visibility = visibility
        if (visibility) {
            opacityClock.let {
                it.from = it.transform(easingFunction)
                it.to = 128.0
                it.start()
            }
            shownAnimationClock.let {
                it.from = shownAnimation.animatedValue
                it.to = 0.0
                it.start()
            }
        }
    }

    fun onRemoteManaUpdate() {
    }
}