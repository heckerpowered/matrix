package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.MatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.EasingMode
import heckerpowered.matrix.client.ui.foundation.animation.ElasticEase
import heckerpowered.matrix.data.language.MatrixLanguage
import net.minecraft.util.math.MathHelper
import java.time.Duration

class ManaBar {
    companion object {
        val manaBarColor = Color(0, 128, 255, 128)
        val usageManaColor = Color(255, 0, 0, 128)
        val costManaColor = Color(128, 0, 0, 128)
    }

    private var privateCurrentMana = 0.0
    private var privateManaUsage = 0.0
    private var privateManaCost = 0.0
    private var autoHide = false

    var maxMana = 100.0
    var currentMana
        get() = privateCurrentMana
        set(value) {
            val newValue = value.coerceIn(0.0..maxMana)
            if (newValue == privateCurrentMana) {
                return
            }
            val lastMana = privateCurrentMana
            privateCurrentMana = newValue

            manaClock.from = lastMana
            manaClock.to = privateCurrentMana
            manaClock.start()
        }

    var manaUsage
        get() = privateManaUsage
        set(value) {
            val newValue = value.coerceIn(0.0..currentMana)
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

    private val manaClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val manaUsageClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val manaCostClock = AnimationClock(Duration.ofMillis(300), 0.0, 1.0)
    private val opacityClock = AnimationClock(Duration.ofMillis(300), 128.0, 0.0)
    private val easingFunction = ElasticEase()

    init {
        easingFunction.easingMode = EasingMode.OUT
        easingFunction.oscillations = 0
    }

    private fun manaPercentage(): Double {
        return manaClock.transform(easingFunction) / maxMana
    }

    fun render(renderer: MatrixUIRenderer) {
        autoHide = false
        manaBarColor.alpha = 128
        renderManaBar(renderer)
        renderManaText(renderer)
    }

    private fun renderManaBar(renderer: MatrixUIRenderer) {
        val minPoint = Point(50.0, 10.0)
        val maxPoint = Point(
            MathHelper.lerp(manaPercentage(), 50.0, renderer.scaledWindowWidth - 50.0),
            25.0
        )

        renderer.renderRectangle(Rectangle(minPoint, maxPoint), manaBarColor)

        val usagePercentage = manaUsageClock.transform(easingFunction) / maxMana
        val usageMinPoint = Point(
            (maxPoint.x - usagePercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            minPoint.y
        )
        renderer.renderRectangle(Rectangle(usageMinPoint, maxPoint), usageManaColor)

        val costPercentage = manaCostClock.transform(easingFunction) / maxMana
        val costMinPoint = Point(
            (usageMinPoint.x - costPercentage * (renderer.scaledWindowWidth - 100)).coerceAtLeast(50.0),
            maxPoint.y
        )
        renderer.renderRectangle(Rectangle(costMinPoint, Point(usageMinPoint.x, maxPoint.y + 3)), costManaColor)
    }

    private fun renderManaText(renderer: MatrixUIRenderer) {
        renderer.render(
            MatrixLanguage.mana,
            Point(55.0, 12.5), Color(255, 255, 255, 255),
            true
        )
    }

    fun renderManaBarAutoHide(renderer: MatrixUIRenderer) {
        manaBarColor.alpha = 128
        if (manaClock.getValue() >= 1.0 && !autoHide) {
            autoHide = true
            opacityClock.start()
        }

        if (autoHide) {
            manaBarColor.alpha = opacityClock.transform(easingFunction).toInt()
        }

        renderManaBar(renderer)
        // renderManaText(renderer)
    }
}