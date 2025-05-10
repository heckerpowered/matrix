package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.Matrix
import heckerpowered.matrix.client.minecraft
import net.minecraft.client.gl.GlUniform
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import org.slf4j.MarkerFactory

private val buffer = MemoryUtil.memAllocFloat(16)

val projectionMatrixProvider = UniformProvider("projectionMatrix") { pointer ->
    buffer.position(0)
    RenderSystem.getProjectionMatrix().get(buffer)
    GL20.glUniformMatrix4fv(pointer, false, buffer)
}

val modelViewMatrixProvider = UniformProvider("modelViewMatrix") { pointer ->
    buffer.position(0)
    RenderSystem.getModelViewMatrix().get(buffer)
    GL20.glUniformMatrix4fv(pointer, false, buffer)
}

val resolutionProvider = UniformProvider("resolution") { pointer ->
    val width = minecraft.window.framebufferWidth.toFloat()
    val height = minecraft.window.framebufferHeight.toFloat()
    GL20.glUniform2f(pointer, width, height)
}

open class UniformProvider(val name: String, val set: (pointer: Int) -> Unit) {
    companion object {
        private val MARKER = MarkerFactory.getMarker("UniformProvider")
    }

    var pointer = -1

    fun init(program: Int) {
        pointer = GlUniform.getUniformLocation(program, name)
        if (pointer == -1) {
            Matrix.LOGGER.error(MARKER, "Cannot find uniform location, name: $name")
        }
    }
}