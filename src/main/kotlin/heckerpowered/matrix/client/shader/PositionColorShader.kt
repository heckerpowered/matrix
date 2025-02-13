package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.Color
import heckerpowered.matrix.core.resourceToString
import org.lwjgl.system.MemoryUtil

object PositionColorShader : Shader(
    resourceToString("/assets/matrix/shaders/position_color.vsh"),
    resourceToString("/assets/matrix/shaders/position_color.fsh"),
    arrayOf(
        modelViewMatrixProvider,
        projectionMatrixProvider,
        UniformProvider("colorModulator") { pointer ->
            RenderSystem.glUniform3(pointer, PositionColorShader.colorBuffer)
        }
    )
) {
    private val colorBuffer = MemoryUtil.memAllocFloat(4)
    var color = Color(1, 1, 1, 1)
        set(value) {
            field = value
            uploadBuffer()
        }

    private fun uploadBuffer() {
        val color = PositionColorShader.color
        val colorBuffer = PositionColorShader.colorBuffer
        colorBuffer.clear()
        colorBuffer.put(color.red / 255F)
        colorBuffer.put(color.green / 255F)
        colorBuffer.put(color.blue / 255F)
        colorBuffer.put(color.alpha / 255F)
    }
}