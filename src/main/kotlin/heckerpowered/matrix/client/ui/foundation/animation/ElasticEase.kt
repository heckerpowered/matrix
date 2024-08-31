package heckerpowered.matrix.client.ui.foundation.animation

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

class ElasticEase : EasingFunction() {
    var oscillations: Int = 3
    var springiness: Double = 3.0

    override fun transformCore(normalizedTime: Double): Double {
        val oscillations = max(0.0, oscillations.toDouble())
        val springiness = max(0.0, this.springiness)
        val exponent = if (springiness == 0.0) {
            normalizedTime
        } else {
            (exp(springiness * normalizedTime) - 1.0) / (exp(springiness) - 1.0)
        }

        return exponent * (sin((Math.PI * 2.0 * oscillations + Math.PI * 0.5) * normalizedTime))
    }
}