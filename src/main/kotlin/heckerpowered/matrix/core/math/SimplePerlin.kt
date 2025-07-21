package heckerpowered.matrix.core.math

import heckerpowered.matrix.core.lerp
import kotlin.math.floor
import kotlin.random.Random

class SimplePerlin(seed: Long) {
    private val permutation = IntArray(512)

    init {
        val source = IntArray(256) { it }
        val random = Random(seed)

        // Fisher–Yates shuffle
        for (i in 255 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = source[i]
            source[i] = source[j]
            source[j] = temp
        }

        // Repeat to simplify index wrapping
        for (i in 0 until 512) {
            permutation[i] = source[i % 256]
        }
    }

    /**
     * Returns a smooth pseudo-random value in [-1, 1] for a given 1D input.
     * @param xInput The input position (usually time).
     */
    fun noise(xInput: Float): Float {
        val baseIndex = floor(xInput).toInt() and 255
        val localX = xInput - floor(xInput) // Fractional part in [0,1)
        val smoothStep = fade(localX)       // Smooth interpolation weight

        val hashA = permutation[baseIndex]
        val hashB = permutation[baseIndex + 1]

        val gradientA = gradient(hashA, localX)
        val gradientB = gradient(hashB, localX - 1f)

        // Linear interpolate between the two gradients
        return lerp(smoothStep, gradientA, gradientB)
    }

    /**
     * Quintic fade function: 6t^5 - 15t^4 + 10t^3
     * This smooths the interpolation so derivatives at boundaries are 0.
     */
    private fun fade(t: Float): Float {
        return t * t * t * (t * (t * 6 - 15) + 10)
    }

    /**
     * Gradient function: returns x or -x based on the hash
     */
    private fun gradient(hash: Int, x: Float): Float {
        return if ((hash and 1) == 0) x else -x
    }
}