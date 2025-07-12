package heckerpowered.matrix.client.render

import org.joml.Matrix4f

object RenderSystem {
    fun createNdcToScreenMatrix(resolutionX: Float, resolutionY: Float): Matrix4f {
        return Matrix4f().set(
            resolutionX / 2f, 0f, 0f, resolutionX / 2f,
            0f, resolutionY / 2f, 0f, resolutionY / 2f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }
}