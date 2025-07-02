package heckerpowered.matrix.client.render.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL11.glBindTexture
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL46

object TentShader {
    var framebufferObject: Int = -1
    var levelOfDetail = .0F

    val tentBlurShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/blur/tent.fsh"),
        arrayOf(
            UniformProvider("framebuffer") { pointer ->
                glActiveTexture(GlConst.GL_TEXTURE0)
                glBindTexture(GlConst.GL_TEXTURE_2D, framebufferObject)
                RenderSystem.glUniform1i(pointer, 0)
            },
            UniformProvider("lod") { pointer ->
                GL46.glUniform1f(pointer, levelOfDetail)
            }
        )
    )

    fun enable(framebufferObject: Int, levelOfDetail: Float) {
        this.framebufferObject = framebufferObject
        this.levelOfDetail = levelOfDetail
        tentBlurShader.enableShader()
    }

    fun disable() {
        tentBlurShader.disableShader()
    }
}