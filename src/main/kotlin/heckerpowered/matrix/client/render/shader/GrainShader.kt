package heckerpowered.matrix.client.render.shader

import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.DissolveShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.opengl.GL46.*

object GrainShader {
    var opacityMask: Int = 0
    val grainShader = BlitShader(
        resourceToString("/assets/matrix/shaders/sobel.vert"),
        resourceToString("/assets/matrix/shaders/post/grain/background_grain.fsh"),
        arrayOf(
            UniformProvider("opacityMask") { pointer ->
                glActiveTexture(GL_TEXTURE1)
                glBindTexture(GL_TEXTURE_2D, opacityMask)
                glUniform1i(pointer, 1)
            },
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, DissolveShader.perlinNoiseTextureId)
                glUniform1i(pointer, 0)
            },
            UniformProvider("grainStrength") { pointer ->
                glUniform1f(pointer, 0.05F)
            }
        )
    )
}