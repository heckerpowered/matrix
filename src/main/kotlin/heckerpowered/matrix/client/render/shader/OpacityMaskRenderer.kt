package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.Framebuffer
import org.lwjgl.opengl.GL46.*

/**
 * A renderer that applies an opacity mask to discard fully transparent or fully black pixels.
 *
 * This class uses a custom fragment shader (`opacity_mask.fsh`) to perform per-pixel discarding based on the
 * following rules:
 *
 * - Discards a pixel if its **alpha channel is 0.0**.
 * - Discards a pixel if **all RGB channels are 0** (i.e., a fully black pixel).
 *
 * It uses two framebuffers:
 * - `opacityMaskFramebuffer`: Provides the mask texture to control pixel visibility.
 * - `colorFramebuffer`: Contains the source color data to be masked.
 *
 * During rendering, the appropriate color attachments from each framebuffer are bound to shader uniforms,
 * and a full-screen blit is performed with the mask applied.
 *
 * @see Shader
 * @see Framebuffer
 * @see UniformProvider
 */
object OpacityMaskRenderer {
    /**
     * The OpenGL texture ID for the color attachment from the opacity mask framebuffer.
     */
    private var opacityMaskColorAttachment: Int = -1

    /**
     * The OpenGL texture ID for the color attachment from the color framebuffer.
     */
    private var colorAttachment: Int = -1

    /**
     * The shader used to apply the opacity mask.
     *
     * It binds two texture inputs:
     * - `opacityMask` bound to texture unit 0.
     * - `colorAttachment` bound to texture unit 1.
     *
     * The actual discard logic is implemented in the `opacity_mask.fsh` shader.
     */
    private val opacityMaskShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/opacity_mask.fsh"),
        arrayOf(
            UniformProvider("colorAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, colorAttachment)
                glUniform1i(pointer, 1)
            },
            UniformProvider("opacityMask") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, opacityMaskColorAttachment)
                glUniform1i(pointer, 0)
            }
        )
    )

    /**
     * Renders the opacity-masked result using the provided framebuffers.
     *
     * This method sets up the shader with appropriate texture bindings from the given framebuffers,
     * enables the shader, performs a full-screen blit, and then disables the shader.
     *
     * @param opacityMaskFramebuffer The framebuffer containing the opacity mask texture.
     * @param colorFramebuffer The framebuffer containing the color texture to be masked.
     */
    fun render(opacityMaskFramebuffer: Framebuffer, colorFramebuffer: Framebuffer) {
        opacityMaskColorAttachment = opacityMaskFramebuffer.colorAttachment
        colorAttachment = colorFramebuffer.colorAttachment
        opacityMaskShader.enableShader()
        BlitShader.blit()
        opacityMaskShader.disableShader()
    }
}

/**
 * Applies an opacity mask from this [Framebuffer] to the specified [colorFramebuffer], using [OpacityMaskRenderer].
 *
 * This infix extension function allows for expressive syntax like:
 * ```
 * maskFramebuffer opacityMask colorFramebuffer
 * ```
 * It renders the contents of [colorFramebuffer] with pixels masked out according to the rules defined
 * in [OpacityMaskRenderer], using this framebuffer as the opacity mask source.
 *
 * @receiver The framebuffer providing the opacity mask (usually grayscale or alpha-based).
 * @param colorFramebuffer The framebuffer whose contents are masked and rendered.
 *
 * @see OpacityMaskRenderer
 */
infix fun Framebuffer.opacityMask(colorFramebuffer: Framebuffer) {
    OpacityMaskRenderer.render(this, colorFramebuffer)
}