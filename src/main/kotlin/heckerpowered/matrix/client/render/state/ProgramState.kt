/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import heckerpowered.matrix.client.shader.Program
import org.lwjgl.opengl.GL20.glUseProgram
import org.lwjgl.opengl.GL46.GL_CURRENT_PROGRAM
import org.lwjgl.opengl.GL46.glGetInteger

class ProgramState(val program: Program, val currentProgram: Int = 0, val isSnapshot: Boolean = false) : RenderPipelineState {
    companion object {
        fun captureSnapshot(shader: Program): ProgramState {
            val program = glGetInteger(GL_CURRENT_PROGRAM)

            return ProgramState(shader, program, true)
        }
    }

    override fun apply(): RenderPipelineSnapshot {
        val snapshot = captureSnapshot(program)

        if (isSnapshot) {
            program.disableShader()
            glUseProgram(currentProgram)
        } else {
            program.enableShader()
        }

        return RenderPipelineSnapshot(snapshot)
    }
}