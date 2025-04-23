package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL46.*

/**
 * A singleton object responsible for applying a radial blur post-processing effect.
 *
 * This renderer uses a custom shader to simulate radial blur by sampling pixels in a
 * radial pattern centered on the screen.
 *
 * ## Usage:
 * - Before calling `radialBlurShader.blit()`, set `colorAttachment`, `strength`, and `samples` as needed.
 *
 * ## Shader Inputs:
 * - `framebuffer`: The input color texture to be blurred.
 * - `strength`: Controls the intensity of the radial blur effect.
 * - `samples`: Determines how many samples are taken along the blur path.
 */
object RadialBlurRenderer : PostProcessEffect {

    /** The OpenGL texture ID of the framebuffer's color attachment to be blurred. */
    override var colorAttachment = 0

    /** Controls how strong the radial blur effect appears. */
    var strength = 1.0F

    /** The number of samples taken to compute the blur effect. Higher = smoother blur. */
    var samples = 10

    /**
     * The shader responsible for executing the radial blur effect.
     *
     * Uses a vertex shader (`sobel.vert`) and a fragment shader (`radial_blur.fsh`) to perform
     * the blur in a post-processing step.
     */
    val radialBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blur/radial_blur.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, colorAttachment)
                glUniform1i(pointer, 0)
            },
            UniformProvider("strength") { pointer ->
                glUniform1f(pointer, strength)
            },
            UniformProvider("samples") { pointer ->
                glUniform1i(pointer, samples)
            }
        )
    )

    override fun blit() {
        radialBlurShader.blit()
    }
}