package heckerpowered.matrix.client.ui.element

import heckerpowered.matrix.client.easingFunction
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.client.render.LegacyMatrixUIRenderer
import heckerpowered.matrix.client.render.Point
import heckerpowered.matrix.client.render.Rectangle
import heckerpowered.matrix.client.ui.foundation.animation.AnimationClock
import heckerpowered.matrix.client.ui.foundation.animation.DoubleAnimation
import heckerpowered.matrix.common.magics.MagicAvailableStatus
import heckerpowered.matrix.common.magics.description
import java.time.Duration

object AvailableStatusTooltip {
    private val opacityClock = AnimationClock(Duration.ofMillis(300), 128.0, 0.0)
    private val shownAnimationClock = AnimationClock(Duration.ofMillis(300), -50.0, .0)
    private val shownAnimation = DoubleAnimation(shownAnimationClock, easingFunction)
    private val opacityAnimation = DoubleAnimation(opacityClock, easingFunction)

    fun show() {
        opacityAnimation.currentValue = 1.0
        shownAnimation.currentValue = 0.0
    }

    fun hide() {
        opacityAnimation.currentValue = 0.0
        shownAnimation.currentValue = -50.0
    }

    fun render(renderer: LegacyMatrixUIRenderer, status: MagicAvailableStatus) {
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

        if (255 * opacityAnimation.animatedValue <= 5) {
            return
        }

        renderer.render(
            status.description,
            Point(
                renderer.scaledWindowWidth / 2 - 125.0 + 5,
                32.5 + shownAnimation.animatedValue
            ),
            Color(255, 255, 255, (255 * opacityAnimation.animatedValue).toInt()),
            true
        )
    }
}