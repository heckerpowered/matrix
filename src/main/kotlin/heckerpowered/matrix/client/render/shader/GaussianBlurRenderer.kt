package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.render.PostProcessRenderer
import heckerpowered.matrix.client.render.post.ScaleSampling
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.joml.Vector2f
import org.lwjgl.opengl.GL46.*

object GaussianBlurRenderer {
    var colorAttachment: Int = 0
    var gaussianKernel: FloatArray = FloatArray(0)
    var direction: Vector2f = Vector2f(1F, 0F)

    val ping = ScaleSampling.createManagedScalingFramebuffer(1.0 / 4)
    val pong = ScaleSampling.createManagedScalingFramebuffer(1.0 / 4)

    val fullPing = PostProcessRenderer.createManagedFramebuffer()
    val fullPong = PostProcessRenderer.createManagedFramebuffer()

    val gaussianBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blur/gaussian_blur.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, colorAttachment)
                glUniform1i(pointer, 0)
            },
            UniformProvider("kernel") { pointer ->
                glUniform1fv(pointer, gaussianKernel)
            },
            UniformProvider("kernelSize") { pointer ->
                glUniform1i(pointer, (gaussianKernel.size - 1).coerceAtLeast(0))
            },
            UniformProvider("direction") { pointer ->
                glUniform2f(pointer, direction.x, direction.y)
            }
        )
    )

    /**
     * Generates a 1D symmetric Gaussian blur kernel for separable sampling.
     *
     * The returned array has length (radius + 1), where:
     *   kernel[0]     = weight for offset 0 (center)
     *   kernel[i>0]   = weight for ±i offsets
     *
     * All weights are normalized so that:
     *   kernel[0] + 2 * sum(kernel[1..radius]) == 1.0
     *
     * @param radius  The blur radius (maximum offset in pixels).
     * @param sigma   The standard deviation of the Gaussian function.
     * @return        FloatArray of size (radius + 1) containing normalized weights.
     */
    fun generateSymmetricGaussianKernel(radius: Int, sigma: Float): FloatArray {
        require(radius >= 1) { "Radius must be at least 1." }
        val kernel = FloatArray(radius + 1)
        val twoSigmaSq = 2f * sigma * sigma

        for (i in 0..radius) {
            val x = i.toFloat()
            kernel[i] = kotlin.math.exp(-(x * x) / twoSigmaSq)
        }

        var sum = kernel[0]
        for (i in 1..radius) {
            sum += 2f * kernel[i]
        }

        for (i in kernel.indices) {
            kernel[i] = kernel[i] / sum
        }

        return kernel
    }

    /**
     * Computes the kernel size and sigma (standard deviation) for a Gaussian blur based on a strength value.
     *
     * The strength controls how strong the blur effect is, ranging from no blur (0.0) to maximum blur (1.0).
     * Both kernel size and sigma will scale smoothly with the strength to ensure a visually consistent transition.
     *
     * @param strength A value between 0.0 and 1.0 indicating blur intensity. 0.0 = no blur, 1.0 = maximum blur.
     * @param maxKernelSize The maximum allowed size for the Gaussian kernel. Must be an odd number. Default is 49.
     * @param maxSigma The maximum allowed sigma (blur softness). Default is 8.0.
     * @return A pair (kernelSize, sigma) where kernelSize is an odd integer >= 1, and sigma is a float >= 0.01.
     *
     * Example:
     * ```
     * val (kernelSize, sigma) = computeBlurParameters(strength = 0.75f)
     * val weights = generateSymmetricGaussianKernel(kernelSize, sigma)
     * ```
     */
    fun computeBlurParameters(
        strength: Float,
        maxKernelSize: Int = 49,
        maxSigma: Float = 8.0f,
    ): Pair<Int, Float> {
        require(strength in 0f..1f) { "Strength must be in [0.0, 1.0]" }

        val minKernelSize = 1
        val minSigma = 0.01f

        // Interpolate kernel size and round to nearest odd number
        var kernelSize = (minKernelSize + (maxKernelSize - minKernelSize) * strength).toInt()
        if (kernelSize % 2 == 0) kernelSize += 1

        // Interpolate sigma
        val sigma = minSigma + (maxSigma - minSigma) * strength

        return kernelSize to sigma
    }

    /**
     * Generates a symmetric 1D Gaussian blur kernel based on blur strength (0.0 to 1.0).
     *
     * This overload automatically computes the appropriate kernel size and sigma based on the given strength.
     * - strength = 0.0 → minimal blur (kernel size = 1, sigma ≈ 0.01)
     * - strength = 1.0 → maximum blur (kernel size = maxKernelSize, sigma = maxSigma)
     *
     * @param strength A normalized blur strength value from 0.0 (no blur) to 1.0 (maximum blur).
     * @param maxKernelSize The maximum kernel size to use when strength = 1.0. Must be an odd number. Default is 49.
     * @param maxSigma The maximum sigma (blur spread) to use when strength = 1.0. Default is 8.0.
     * @return A normalized FloatArray representing a symmetric Gaussian kernel.
     *
     * Example:
     * ```
     * val kernel = generateSymmetricGaussianKernel(0.75f, maxKernelSize = 31, maxSigma = 5.0f)
     * ```
     */
    fun generateSymmetricGaussianKernel(
        strength: Float,
        maxKernelSize: Int = 49,
        maxSigma: Float = 8.0f,
    ): FloatArray {
        val (kernelSize, sigma) = computeBlurParameters(strength, maxKernelSize, maxSigma)
        return generateSymmetricGaussianKernel(kernelSize, sigma)
    }
}