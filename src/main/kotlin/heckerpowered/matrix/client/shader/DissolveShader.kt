package heckerpowered.matrix.client.shader

import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.texture.ResourceTexture
import org.lwjgl.opengl.GL46.*
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

class DissolveShader : AutoCloseable {
    private val program by lazy {
        Program(
            ResourceShader("/assets/matrix/shaders/position_texture_color.vsh", GL_VERTEX_SHADER),
            ResourceShader("/assets/matrix/shaders/noise_mask.fsh", GL_FRAGMENT_SHADER),
            uniforms = arrayOf(
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
                    glUniform1f(pointer, System.nanoTime().nanoseconds.toDouble(DurationUnit.SECONDS).toFloat() % 10)
                }
            )
        )
    }

    companion object {
        private val perlinNoiseTexture = ResourceTexture(Matrix.identifier("textures/noise/perlin_noise.png"))
        val perlinNoiseTextureId by lazy { loadPerlinNoiseTexture() }

        private fun loadPerlinNoiseTexture(): Int {
            perlinNoiseTexture.load(minecraft.resourceManager)

            glBindTexture(GL_TEXTURE_2D, perlinNoiseTexture.glId)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT)

            return perlinNoiseTexture.glId
        }
    }

    val noiseTexture by lazy { perlinNoiseTextureId }
    var dissolveFactor: Float = 1.0f
    val normalTexture: Int = 0

    var resolutionX: Float = 1.0F
    var resolutionY: Float = 1.0F

    fun enableShader() {
        program.enableShader()
    }

    fun disableShader() {
        program.disableShader()
    }

    override fun close() {
        program.close()
    }
}