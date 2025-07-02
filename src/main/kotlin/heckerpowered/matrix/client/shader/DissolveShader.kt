package heckerpowered.matrix.client.shader

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.texture.ResourceTexture
import org.lwjgl.opengl.GL46.*
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class DissolveShader : AutoCloseable {
    private val shader = Shader(
        resourceToString("/assets/matrix/shaders/position_texture_color.vsh"),
        resourceToString("/assets/matrix/shaders/noise_mask.fsh"),
        arrayOf(
            modelViewMatrixProvider,
            projectionMatrixProvider,
            UniformProvider("noiseTexture") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, noiseTexture)
                glUniform1i(pointer, 0)
            },
            UniformProvider("dissolveFactor") { pointer ->
                glUniform1f(pointer, dissolveFactor)
            },
            UniformProvider("resolution") { pointer ->
                glUniform2f(pointer, resolutionX, resolutionY)
            },
            UniformProvider("time") { pointer ->
                glUniform1f(pointer, System.nanoTime().nanoseconds.toDouble(DurationUnit.SECONDS).toFloat())
            }
        )
    )

    companion object {
        private val perlinNoiseTexture = ResourceTexture(Matrix.identifier("textures/perlin_noise.png"))
        var perlinNoiseTextureId: Int = 0
        private fun loadPerlinNoiseTexture() {
            perlinNoiseTexture.load(minecraft.resourceManager)
            perlinNoiseTextureId = perlinNoiseTexture.glId

            glBindTexture(GL_TEXTURE_2D, perlinNoiseTextureId)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT)
        }

        init {
            loadPerlinNoiseTexture()
        }
    }

    var noiseTexture: Int = perlinNoiseTextureId
    var dissolveFactor: Float = 1.0f
    val normalTexture: Int = 0

    var resolutionX: Float = 1.0F
    var resolutionY: Float = 1.0F

    fun enableShader() {
        shader.enableShader()
    }

    fun disableShader() {
        shader.disableShader()
    }

    override fun close() {
        shader.close()
    }
}