package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.GlUniform
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil

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

open class UniformProvider(val name: String, val set: (pointer: Int) -> Unit) {
    var pointer = -1

    fun init(program: Int) {
        pointer = GlUniform.getUniformLocation(program, name)
    }
}