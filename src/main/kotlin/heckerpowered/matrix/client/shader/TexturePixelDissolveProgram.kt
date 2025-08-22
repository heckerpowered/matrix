package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.texture.ResourceTexture
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER
import org.lwjgl.opengl.GL20.GL_VERTEX_SHADER

object TexturePixelDissolveProgram : BlitProgram(
    ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
    ResourceShader("/assets/matrix/shaders/texture_pixel_dissolve.fsh", GL_FRAGMENT_SHADER),
    uniforms = arrayOf(
        UniformProvider("noiseTexture") { pointer ->
            GL20.glActiveTexture(GlConst.GL_TEXTURE0)
            GL20.glBindTexture(GL20.GL_TEXTURE_2D, TexturePixelDissolveProgram.noiseTexture)
            GL20.glUniform1i(pointer, 0)
        },
        UniformProvider("normalTexture") { pointer ->
            GL20.glActiveTexture(GlConst.GL_TEXTURE1)
            GL20.glBindTexture(GL20.GL_TEXTURE_2D, TexturePixelDissolveProgram.normalTexture)
            GL20.glUniform1i(pointer, 1)
        },
        UniformProvider("dissolveFactor") { pointer ->
            GL20.glUniform1f(pointer, TexturePixelDissolveProgram.dissolveFactor)
        }
    )
) {
    private val perlinNoiseTexture = ResourceTexture(Matrix.identifier("textures/perlin_noise.png"))
    private var perlinNoiseTextureId: Int = 0
    private fun loadPerlinNoiseTexture() {
        perlinNoiseTexture.load(minecraft.resourceManager)
        perlinNoiseTextureId = perlinNoiseTexture.glId
    }

    init {
        loadPerlinNoiseTexture()
    }

    var noiseTexture: Int = perlinNoiseTextureId
    var normalTexture: Int = 0
    var dissolveFactor: Float = 1.0f
}