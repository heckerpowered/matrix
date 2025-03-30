package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import heckerpowered.matrix.core.resourceToString
import net.minecraft.client.texture.ResourceTexture
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL31

class DissolveShader : AutoCloseable {
    private val shader = Shader(
        resourceToString("/assets/matrix/shaders/position_texture_color.vsh"),
        resourceToString("/assets/matrix/shaders/noise_mask.fsh"),
        arrayOf(
            modelViewMatrixProvider,
            projectionMatrixProvider,
            UniformProvider("noiseTexture") { pointer ->
                GL31.glActiveTexture(GlConst.GL_TEXTURE0)
                GL31.glBindTexture(GL31.GL_TEXTURE_2D, noiseTexture)
                GL20.glUniform1i(pointer, 0)
            },
            UniformProvider("dissolveFactor") { pointer ->
                GL20.glUniform1f(pointer, dissolveFactor)
            }
        )
    )

    companion object {
        private val perlinNoiseTexture = ResourceTexture(Matrix.identifier("textures/perlin_noise.png"))
        var perlinNoiseTextureId: Int = 0
        private fun loadPerlinNoiseTexture() {
            perlinNoiseTexture.load(minecraft.resourceManager)
            perlinNoiseTextureId = perlinNoiseTexture.glId
        }

        init {
            loadPerlinNoiseTexture()
        }
    }

    var noiseTexture: Int = perlinNoiseTextureId
    var dissolveFactor: Float = 1.0f
    val normalTexture: Int = 0

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