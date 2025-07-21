package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.core.math.SimplePerlin
import org.joml.Matrix4f
import org.joml.Vector2f
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

object CameraShake {
    var perlin = SimplePerlin(Random.nextLong())
    private var shakeStartTime: Duration = Duration.ZERO
    var shakeDuration: Duration = Duration.ZERO
        set(value) {
            shakeStartTime = System.nanoTime().nanoseconds
            field = value
        }
    var strength: Float = 0.0f
    var enableCameraShake = false

    @JvmStatic
    fun applyCameraShake(viewMatrix: Matrix4f) {
        if (!enableCameraShake) {
            return
        }

        val shakeOffset = getShakeOffset()
        viewMatrix.translate(shakeOffset.x, shakeOffset.y, 0.0F)
    }

    fun getShakeOffset(): Vector2f {
        val elapsedTime = (System.nanoTime().nanoseconds - shakeStartTime)
            .toLong(DurationUnit.NANOSECONDS)
            .coerceAtMost(shakeDuration.toLong(DurationUnit.NANOSECONDS))
        val time = elapsedTime.nanoseconds.toDouble(DurationUnit.SECONDS).toFloat()

        val decay = 1.0F - (elapsedTime.nanoseconds / shakeDuration).toFloat()
        val speed = 10F

        val x = (perlin.noise(time * speed) * 2F - 1F) * strength * decay
        val y = (perlin.noise((time + 100) * speed) * 2f - 1f) * strength * decay
        return Vector2f(x, y)
    }

    fun shake(duration: Duration = 50.milliseconds, strength: Float = 1.0F) {
        this.strength = strength
        shakeDuration = duration
        enableCameraShake = true
        perlin = SimplePerlin(Random.nextLong())
    }
}