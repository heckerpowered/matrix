package heckerpowered.matrix.client.render.post

import heckerpowered.matrix.client.shader.*
import heckerpowered.matrix.client.ui.foundation.animation.SimpleDoubleAnimation
import heckerpowered.matrix.core.times
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL46.*

object ShockwaveRenderer {
    var depthAttachment: Int = -1

    var wavePosition: Vector3f = Vector3f()
    var waveColor = Vector4f(0.1F, 0.5F, 1.0F, 1.0F) * 4.0F
    var waveRadius = SimpleDoubleAnimation()
    var waveSize = SimpleDoubleAnimation()

    val shockwaveShader = BlitProgram(
        ResourceShader("/assets/matrix/shaders/sobel.vert", GL_VERTEX_SHADER),
        ResourceShader("/assets/matrix/shaders/post/shockwave.fsh", GL_FRAGMENT_SHADER),
        uniforms = arrayOf(
            UniformProvider("depthAttachment") { pointer ->
                glActiveTexture(GL_TEXTURE0)
                glBindTexture(GL_TEXTURE_2D, depthAttachment)
                glUniform1i(pointer, 0)
            },
            inverseProjectionMatrixProvider,
            inverseViewMatrixProvider,
            UniformProvider("wavePosition") { pointer ->
                glUniform3f(pointer, wavePosition.x, wavePosition.y, wavePosition.z)
            },
            UniformProvider("waveColor") { pointer ->
                glUniform4f(pointer, waveColor.x, waveColor.y, waveColor.z, waveColor.w)
            },
            UniformProvider("waveRadius") { pointer ->
                glUniform1f(pointer, waveRadius.animatedValue.toFloat())
            },
            UniformProvider("waveSize") { pointer ->
                glUniform1f(pointer, waveSize.animatedValue.toFloat())
            }
        )
    )
}