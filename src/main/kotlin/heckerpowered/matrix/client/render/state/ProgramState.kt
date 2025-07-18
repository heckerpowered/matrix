package heckerpowered.matrix.client.render.state

import heckerpowered.matrix.client.shader.Shader
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL46.GL_CURRENT_PROGRAM
import org.lwjgl.opengl.GL46.glGetInteger

class ProgramState(val shader: Shader, val currentProgram: Int = 0, val isSnapshot: Boolean = false) : RenderPipelineState {
    companion object {
        fun captureSnapshot(shader: Shader): ProgramState {
            val program = glGetInteger(GL_CURRENT_PROGRAM)

            return ProgramState(shader, program, true)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot(shader)

        if (isSnapshot) {
            shader.disableShader()
            glUseProgram(currentProgram)
        } else {
            shader.enableShader()
        }

        return RenderPipelineSnapshot(snapshot)
    }
}