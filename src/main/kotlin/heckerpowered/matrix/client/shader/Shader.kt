package heckerpowered.matrix.client.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import heckerpowered.matrix.client.shader.component.ShaderComponent
import net.minecraft.client.gl.GlProgramManager
import org.lwjgl.opengl.GL46.*
import java.io.Closeable

open class Shader(
    vertexShaderPath: String?,
    fragmentShaderPath: String?,
    private val uniforms: Array<UniformProvider> = emptyArray(),
    val components: Array<ShaderComponent> = emptyArray(),
    tessellationControlShaderPath: String? = null,
    tessellationEvaluationShaderPath: String? = null,
    geometryShaderPath: String? = null,
) : Closeable {
    var program = 0

    init {
        val vertexShader = compileShader(vertexShaderPath, GL_VERTEX_SHADER)
        val fragmentShader = compileShader(fragmentShaderPath, GL_FRAGMENT_SHADER)
        val tessellationControlShader = compileShader(tessellationControlShaderPath, GL_TESS_CONTROL_SHADER)
        val tessellationEvaluationShader = compileShader(tessellationEvaluationShaderPath, GL_TESS_EVALUATION_SHADER)
        val geometryShader = compileShader(geometryShaderPath, GL_GEOMETRY_SHADER)

        program = GlStateManager.glCreateProgram()

        attachShader(program, vertexShader)
        attachShader(program, fragmentShader)
        attachShader(program, tessellationControlShader)
        attachShader(program, tessellationEvaluationShader)
        attachShader(program, geometryShader)

        components.forEach { component ->
            component.init(program)
        }

        GlStateManager.glLinkProgram(program)

        if (GlStateManager.glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            val log = GlStateManager.glGetProgramInfoLog(program, 1024)
            error("Failed to compile shader: $log")
        }

        deleteShader(vertexShader)
        deleteShader(fragmentShader)
        deleteShader(tessellationEvaluationShader)
        deleteShader(tessellationControlShader)
        deleteShader(geometryShader)

        uniforms.forEach { uniform ->
            uniform.init(program)
        }
    }

    protected fun attachShader(program: Int, shader: Int?) {
        if (shader != null) {
            GlStateManager.glAttachShader(program, shader)
        }
    }

    protected fun deleteShader(shader: Int?) {
        if (shader != null) {
            GlStateManager.glDeleteShader(shader)
        }
    }

    protected fun compileShader(source: String?, type: Int): Int? {
        if (source == null) {
            return null
        }
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
        components.forEach { component ->
            component.delete()
        }
    }

    fun enableShader() {
        GlProgramManager.useProgram(program)
        uniforms.forEach { uniform -> uniform.set(uniform.pointer) }
        components.filter { it.enabled }.forEach { it.enable() }
    }

    fun disableShader() {
        components.filter { it.enabled }.forEach { it.disable() }
        GlProgramManager.useProgram(0)
    }
}