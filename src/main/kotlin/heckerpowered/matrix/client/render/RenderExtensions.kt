package heckerpowered.matrix.client.render

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.GlStateManager.Viewport
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.texture.NativeImage
import org.joml.Vector4f
import org.lwjgl.opengl.GL46.*
import java.io.File

fun Framebuffer.blit(target: Framebuffer, mask: Int, filter: Int) {
    glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo)
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, target.fbo)
    glBlitFramebuffer(
        0, 0, textureWidth, textureHeight,
        0, 0, target.textureWidth, target.textureHeight,
        mask, filter
    )
}

private var primaryFramebuffer: Framebuffer? = null
private var secondaryFramebuffer: Framebuffer? = null
var colorMultiplier = Vector4f(1.0F, 1.0F, 1.0F, 1.0F)
private val colorFusionShader by lazy {
    BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/color_fusion.fsh"),
        arrayOf(
            UniformProvider("primaryFramebuffer") { pointer ->
                val framebuffer = primaryFramebuffer ?: return@UniformProvider

                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                glUniform1i(pointer, 0)
            },
            UniformProvider("secondaryFramebuffer") { pointer ->
                val framebuffer = secondaryFramebuffer ?: return@UniformProvider

                glActiveTexture(GlConst.GL_TEXTURE0 + 1)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                glUniform1i(pointer, 1)
            },
            UniformProvider("colorMultiplier") { pointer ->
                glUniform4f(pointer, colorMultiplier.x, colorMultiplier.y, colorMultiplier.z, colorMultiplier.w)
            }
        )
    )
}

private val blendScreenShader by lazy {
    BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blend_screen.fsh"),
        arrayOf(
            UniformProvider("primaryFramebuffer") { pointer ->
                val framebuffer = primaryFramebuffer ?: return@UniformProvider

                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                glUniform1i(pointer, 0)
            },
            UniformProvider("secondaryFramebuffer") { pointer ->
                val framebuffer = secondaryFramebuffer ?: return@UniformProvider

                glActiveTexture(GlConst.GL_TEXTURE0 + 1)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
                glUniform1i(pointer, 1)
            }
        )
    )
}

val tentBlurShader = BlitShader(
    resourceToString("/assets/matrix/shaders/sobel.vert"),
    resourceToString("/assets/matrix/shaders/post/blur/tent.fsh"),
    arrayOf(UniformProvider("framebuffer") { pointer ->
        val framebuffer = primaryFramebuffer ?: return@UniformProvider

        glActiveTexture(GlConst.GL_TEXTURE0)
        glBindTexture(GlConst.GL_TEXTURE_2D, framebuffer.colorAttachment)
        RenderSystem.glUniform1i(pointer, 0)
    })
)

infix fun Framebuffer.blend(other: Framebuffer) {
    primaryFramebuffer = this
    secondaryFramebuffer = other
    colorFusionShader.blit()
}

infix fun Framebuffer.blendScreen(other: Framebuffer) {
    primaryFramebuffer = this
    secondaryFramebuffer = other
    blendScreenShader.blit()
}

fun Framebuffer.draw(drawFunction: () -> Unit) {
    val previousFramebuffer = GlStateManager.getBoundFramebuffer()
    beginWrite(true)
    drawFunction()
    endWrite()
    GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, previousFramebuffer)
}

infix fun Framebuffer.tent(other: Framebuffer): Framebuffer {
    primaryFramebuffer = this
    other.draw {
        tentBlurShader.blit()
    }
    return other
}

fun Framebuffer.inplaceTent(): Framebuffer {
    this tent this
    return this
}

infix fun Framebuffer.copyTo(other: Framebuffer) {
    blit(other, GL_COLOR_BUFFER_BIT, GL_LINEAR)
}

infix fun Framebuffer.copyDepthTo(other: Framebuffer) {
    blit(other, GL_DEPTH_BUFFER_BIT, GL_LINEAR)
}

infix fun Framebuffer.linearCopyTo(other: Framebuffer) {
    blit(other, GL_COLOR_BUFFER_BIT, GL_LINEAR)
}

infix fun Framebuffer.nearestCopyTo(other: Framebuffer) {
    blit(other, GL_COLOR_BUFFER_BIT, GL_NEAREST)
}

/**
 * Calculates the recommend number of mipmap levels for a given texture size.
 *
 * Mipmap levels are calculated based on the longest edge (width or height).
 * The number of levels is defined as:
 *
 * `floor(log2(max(width, height))) + 1`
 *
 * This represents the total numbers of mip levels including the base level (origin size).
 * where each subsequent level is half the size of the previous one, until reaching 1x1.
 *
 * This implementation uses bit-level operation to avoid floating-point math,
 * which is faster and more efficient on the JVM.
 *
 * @param width the width of the texture
 * @param height the height of the texture
 * @return the number of mipmap levels needed.
 * @author heckerpowered
 */
fun recommendMipLevel(width: Int, height: Int): Int {
    val size = maxOf(width, height)

    // Explantation:
    // The number of mip levels is: floor(log2(size)) + 1
    //
    // (size - 1).countLeadingZeroBits() gives the number of zero bits before the first 1 bit.
    // For example: 256 (0B100000000) has 23 leading zeros in a 32-bit int.
    //
    // So: Int.SIZE_BITS - (size - 1).countLeadingZeroBits()
    //   = 32 - countLeadingZeroBits(size - 1)
    //   = floor(log2(size)) + 1
    //
    // This avoids using log2 and is equivalent for all positive integers.
    return Int.SIZE_BITS - (size - 1).countLeadingZeroBits()
}

/**
 * Calculates the recommend number of mipmap levels for this framebuffer.
 *
 * Mipmap levels are calculated based on the longest edge (width or height).
 * The number of levels is defined as:
 *
 * `floor(log2(max(width, height))) + 1`
 *
 * This represents the total numbers of mip levels including the base level (origin size).
 * where each subsequent level is half the size of the previous one, until reaching 1x1.
 *
 * This implementation uses bit-level operation to avoid floating-point math,
 * which is faster and more efficient on the JVM.
 *
 * @return the number of mipmap levels needed.
 * @author heckerpowered
 */
fun Framebuffer.recommendMipLevel(): Int {
    return recommendMipLevel(textureWidth, textureHeight)
}

fun Framebuffer.dump(levelOfDetail: Int = 0, generateMipmap: Boolean = true) {
    dump(hashCode().toString(), levelOfDetail, generateMipmap)
}

fun Framebuffer.dump(name: String, levelOfDetail: Int = 0, generateMipmap: Boolean = true) {
    RenderSystem.bindTexture(colorAttachment)

    if (generateMipmap) {
        glGenerateMipmap(GL_TEXTURE_2D)
    }
    val width = glGetTexLevelParameteri(GL_TEXTURE_2D, levelOfDetail, GL_TEXTURE_WIDTH)
    val height = glGetTexLevelParameteri(GL_TEXTURE_2D, levelOfDetail, GL_TEXTURE_HEIGHT)

    if (width <= 0 || height <= 0) {
        checkGLError("dump_framebuffer")
        return
    }

    NativeImage(width, height, false).use { nativeImage ->
        nativeImage.loadFromTextureImage(levelOfDetail, false)
        nativeImage.mirrorVertically()

        val file = File("screenshots")
        file.mkdir()

        val filename = "framebuffer_dump_${name}.png"
        nativeImage.writeTo(File(file, filename))
    }
}

/**
 * Executes the given [action] while preserving the currently bound framebuffer and viewport state.
 *
 * This function backs up the current framebuffer binding and viewport dimensions,
 * then invokes the specified [action]. After the action completes, the original framebuffer
 * and viewport state are restored. This ensures that temporary framebuffer or viewport changes
 * inside [action] do not leak outside the function scope.
 *
 * This is especially useful when performing off-screen rendering or rendering to custom framebuffers,
 * as it guarantees rendering state isolation.
 *
 * Example usage:
 * ```
 * framebufferGuard {
 *     glBindFramebuffer(GL_FRAMEBUFFER, customFramebuffer)
 *     GlStateManager._viewport(0, 0, width, height)
 *     renderSomething()
 * }
 * // Original framebuffer and viewport are now restored
 * ```
 *
 * @param action The block of code to execute within the guarded framebuffer and viewport state.
 */
fun framebufferGuard(action: () -> Unit) {
    val previousBindingFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING)
    val previousViewportX = Viewport.getX()
    val previousViewportY = Viewport.getY()
    val previousViewportWidth = Viewport.getWidth()
    val previousViewportHeight = Viewport.getHeight()

    action()

    glBindFramebuffer(GL_FRAMEBUFFER, previousBindingFramebuffer)
    GlStateManager._viewport(previousViewportX, previousViewportY, previousViewportWidth, previousViewportHeight)
}

fun checkGLError(tag: String = "") {
    val err = glGetError()
    if (err != GL_NO_ERROR) {
        println("OpenGL Error at $tag: 0x${err.toString(16)}")
    }
}