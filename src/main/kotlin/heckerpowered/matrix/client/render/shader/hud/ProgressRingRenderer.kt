package heckerpowered.matrix.client.render.shader.hud

import heckerpowered.matrix.client.shader.BlitShader
import heckerpowered.matrix.client.shader.UniformProvider
import heckerpowered.matrix.client.shader.modelViewMatrixProvider
import heckerpowered.matrix.client.shader.projectionMatrixProvider
import heckerpowered.matrix.core.resourceToString
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.opengl.GL46

object ProgressRingRenderer {
    /**
     * The progress of the progress ring, between 0.0F and 1.0F, inclusive.
     */
    var progress: Float = 1.0F

    /**
     * The radius of the progress ring in normalized device coordinates (NDC).
     *
     * 0.5 radius = full screen
     */
    var radius: Float = 0.5F

    /**
     * The thickness of the progress ring in normalized device coordinates (NDC).
     * From the edge of the ring to the center of the ring.
     */
    var thickness: Float = 0.1F

    /**
     * The center of the progress ring in normalized device coordinates (NDC).
     */
    var center: Vector2f = Vector2f(0.5F, 0.5F)

    /**
     * The color of the progress ring.
     */
    var color: Vector4f = Vector4f(1.0F)

    val progressRingShader = BlitShader(
        resourceToString("/assets/matrix/shaders/position_texture.fsh"),
        resourceToString("/assets/matrix/shaders/post/hud/progress_ring.fsh"),
        arrayOf(
            modelViewMatrixProvider,
            projectionMatrixProvider,
            UniformProvider("progress") { pointer ->
                GL46.glUniform1f(pointer, progress)
            },
            UniformProvider("radius") { pointer ->
                GL46.glUniform1f(pointer, radius)
            },
            UniformProvider("thickness") { pointer ->
                GL46.glUniform1f(pointer, thickness)
            },
            UniformProvider("center") { pointer ->
                GL46.glUniform2f(pointer, center.x, center.y)
            },
            UniformProvider("color") { pointer ->
                GL46.glUniform4f(pointer, color.x, color.y, color.z, color.w)
            }
        )
    )
}