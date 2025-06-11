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
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.max

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

private val colorFusionFilterShader by lazy {
    BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/color_filter_fusion.fsh"),
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

infix fun Framebuffer.filterBlend(other: Framebuffer) {
    primaryFramebuffer = this
    secondaryFramebuffer = other
    colorFusionFilterShader.blit()
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

fun recommendMipLevel(width: Int, height: Int): Int {
    return floor(log2(max(width, height).toDouble())).toInt() + 1
}

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

fun spoofFramebuffer(action: () -> Unit) {
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