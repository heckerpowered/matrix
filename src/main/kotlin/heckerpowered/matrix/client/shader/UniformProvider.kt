package heckerpowered.matrix.client.shader

import net.minecraft.client.gl.GlUniform

open class UniformProvider(val name: String, val set: (pointer: Int) -> Unit) {
    var pointer = -1

    fun init(program: Int) {
        pointer = GlUniform.getUniformLocation(program, name)
    }
}