package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.Shader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL46.*

object TextureDissolveShader {
    var colorAttachment: Int = 0
    var dissolveFactor: Float = 0F

    val shader = Shader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/dissolve/texture_dissolve.fsh"),
        arrayOf(
            UniformProvider("colorAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, colorAttachment)
                glUniform1i(pointer, 1)
            },
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                glUniform1i(pointer, 0)
            },
            UniformProvider("dissolveFactor") { pointer ->
                glUniform1f(pointer, dissolveFactor)
            }
        )
    )
}