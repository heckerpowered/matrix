package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import java.io.Closeable

open class Shader(
    vertexProgramPath: String,
    fragmentProgramPath: String,
    private val uniforms: Array<UniformProvider> = emptyArray()
) : Closeable {
    var program = 0

    init {
        val vertexProgram = compileShader(vertexProgramPath, GlConst.GL_VERTEX_SHADER)
        val fragProgram = compileShader(fragmentProgramPath, GlConst.GL_FRAGMENT_SHADER)

        program = GlStateManager.glCreateProgram()

        GlStateManager.glAttachShader(program, vertexProgram)
        GlStateManager.glAttachShader(program, fragProgram)
        GlStateManager.glLinkProgram(program)

        if (GlStateManager.glGetProgrami(program, GlConst.GL_LINK_STATUS) == GlConst.GL_FALSE) {
            val log = GlStateManager.glGetShaderInfoLog(program, 1024)
            error("Failed to compile shader: $log")
        }

        GlStateManager.glDeleteShader(vertexProgram)
        GlStateManager.glDeleteShader(fragProgram)

        uniforms.forEach { uniform ->
            uniform.init(program)
        }
    }

    private fun compileShader(source: String, type: Int): Int {
        val shader = GlStateManager.glCreateShader(type)
        GlStateManager.glShaderSource(shader, listOf(source))
        GlStateManager.glCompileShader(shader)

        if (GlStateManager.glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
            val log = GlStateManager.glGetShaderInfoLog(shader, 1024)
            error("Failed to compile shader: $log")
        }

        return shader
    }

    override fun close() {
        GlStateManager.glDeleteProgram(program)
    }

    fun enableShader() {
        GlStateManager._glUseProgram(program)
        uniforms.forEach { uniform ->
            uniform.init(program)
        }
    }

    fun disableShader() {
        GlStateManager._glUseProgram(0)
    }
}