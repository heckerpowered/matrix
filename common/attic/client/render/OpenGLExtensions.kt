/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import heckerpowered.matrix.client.minecraft
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.glGetActiveUniform
import org.lwjgl.opengl.GL31.glGetActiveUniformBlockiv
import org.lwjgl.opengl.GL31.glGetActiveUniformsiv
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

object OpenGLExtensions {
    @JvmStatic
    fun clearGLError() {
        glGetError()
    }

    @JvmStatic
    fun checkGLError(handler: (Int) -> Unit) {
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            handler(error)
        }
    }

    @JvmStatic
    fun getErrorName(error: Int) = when (error) {
        GL_NO_ERROR -> "GL_NO_ERROR"
        GL_INVALID_ENUM -> "GL_INVALID_ENUM"
        GL_INVALID_VALUE -> "GL_INVALID_VALUE"
        GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
        GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
        GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW"
        GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW"
        else -> "Unknown error $error"
    }

    @JvmStatic
    fun getErrorDescription(error: Int) = when (error) {
        GL_NO_ERROR -> "No error has been recorded."
        GL_INVALID_ENUM -> "An unacceptable value is specified for an enumerated argument. The offering command is ignored and has no other side effect."
        GL_INVALID_VALUE -> "A numeric argument is out of range. The offering command is ignored and has no other side effect."
        GL_INVALID_OPERATION -> "The specified operation is not allowed in the current state. The offering command is ignored and has no other side effect."
        GL_INVALID_FRAMEBUFFER_OPERATION -> "The framebuffer object is not complete. The offering command is ignored and has no side effect."
        GL_OUT_OF_MEMORY -> "There is no enough memory left to execute the command. The state of the GL is undefined."
        GL_STACK_UNDERFLOW -> "An attempt has been made to perform an operation that would cause an internal stack to underflow."
        GL_STACK_OVERFLOW -> "An attempt has been made to perform an operation that would cause an internal stack to overflow."
        else -> "Unknown error occurred: ${getErrorName(error)}"
    }

    fun fastCheck(name: String) {
        checkGLError { error ->
            val message = getErrorName(error)
            val description = getErrorDescription(error)
            println("Error at $name: ($message): $description")
        }
    }

    fun getFramebufferStatusName(status: Int) = when (status) {
        GL_FRAMEBUFFER_COMPLETE -> "GL_FRAMEBUFFER_COMPLETE"
        GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED"
        GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"
        GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"
        GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"
        GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"
        GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED"
        GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"
        GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS"
        else -> "Unknown framebuffer status $status"
    }

    @JvmStatic
    fun getFramebufferStatusDescription(status: Int): String = when (status) {
        GL_FRAMEBUFFER_COMPLETE -> "The framebuffer is complete."
        GL_FRAMEBUFFER_UNDEFINED -> "The framebuffer is the default read or draw framebuffer, but the default framebuffer does not exist."
        GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "One or more framebuffer attachment points are framebuffer incomplete."
        GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "The framebuffer does not have at least one image attached to it."
        GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "The value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for any color attachment point(s) named by GL_DRAW_BUFFERi."
        GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_READ_BUFFER is not GL_NONE and the value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for the color attachment point named by GL_READ_BUFFER."
        GL_FRAMEBUFFER_UNSUPPORTED -> "The combination of internal formats of the attached images violates an implementation-dependent set of restrictions."
        GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "The value of GL_TEXTURE_FIXED_SAMPLE_LOCATIONS is not the same for all attached textures; or, if the attached images are a mix of renderbuffers and textures, the value of GL_TEXTURE_FIXED_SAMPLE_LOCATIONS is not GL_TRUE for all attached textures."
        GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "One or more framebuffer attachments are layered while any populated attachment is not, or not all populated color attachments are from textures of the same target."
        0 -> "(An error occurred while checking framebuffer status) "
        else -> "Unknown framebuffer status: $status"
    }

    @JvmStatic
    fun getPackedPixelDataTypeForFormat(format: Int): Int {
        return when (format) {
            // Use GL_FLOAT for all float-based formats (even half-float internal)
            GL_RGBA16F, GL_RGB16F, GL_RG16F, GL_R16F,
            GL_RGBA32F, GL_RGB32F, GL_RG32F, GL_R32F,
            GL_DEPTH_COMPONENT32F,
                -> GL_FLOAT

            // Normalized formats
            GL_RGBA8, GL_RGB8, GL_RG8, GL_R8 -> GL_UNSIGNED_BYTE

            // Integer formats
            GL_RGBA8UI, GL_RGB8UI, GL_RG8UI, GL_R8UI -> GL_UNSIGNED_BYTE
            GL_RGBA16UI, GL_RGB16UI, GL_RG16UI, GL_R16UI -> GL_UNSIGNED_SHORT
            GL_RGBA32UI, GL_RGB32UI, GL_RG32UI, GL_R32UI -> GL_UNSIGNED_INT

            // Depth
            GL_DEPTH_COMPONENT24 -> GL_UNSIGNED_INT
            GL_DEPTH_COMPONENT16 -> GL_UNSIGNED_SHORT

            else -> throw IllegalArgumentException("Unsupported format: 0x${format.toString(16)}")
        }
    }

    @JvmStatic
    fun getBytesPerPixel(format: Int, type: Int): Int {
        return when (format) {
            GL_RGBA -> when (type) {
                GL_UNSIGNED_BYTE -> 4
                GL_UNSIGNED_SHORT -> 8
                else -> throw IllegalArgumentException("Unsupported type for GL_RGBA")
            }

            GL_RGB -> when (type) {
                GL_UNSIGNED_BYTE -> 3
                GL_UNSIGNED_SHORT -> 6
                else -> throw IllegalArgumentException("Unsupported type for GL_RGB")
            }

            GL_RG -> when (type) {
                GL_UNSIGNED_BYTE -> 2
                GL_UNSIGNED_SHORT -> 4
                else -> throw IllegalArgumentException("Unsupported type for GL_RG")
            }

            GL_RED -> when (type) {
                GL_UNSIGNED_BYTE -> 1
                GL_UNSIGNED_SHORT -> 2
                else -> throw IllegalArgumentException("Unsupported type for GL_RED")
            }

            else -> throw IllegalArgumentException("Unsupported format: 0x${format.toString(16)}")
        }
    }

    fun getUniformBlockUniforms(program: Int, uniformBlockIndex: Int): Int {
        return glGetActiveUniformBlocki(program, uniformBlockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS)
    }

    fun getUniformBlockIndices(program: Int, uniformBlockIndex: Int, count: Int): IntArray {
        val indices = IntArray(count)
        glGetActiveUniformBlockiv(program, uniformBlockIndex, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, indices)
        return indices
    }

    fun getUniformBlockUniformNames(program: Int, count: Int): Array<String> {
        val maxLength = glGetProgrami(program, GL_ACTIVE_UNIFORM_MAX_LENGTH)
        val length = IntArray(1)
        val size = IntArray(1)
        val type = IntArray(1)

        MemoryStack.stackPush().use { memoryStack ->
            val nameBuffer = memoryStack.malloc(maxLength)
            return Array(count) { index ->
                glGetActiveUniform(program, index, length, size, type, nameBuffer)
                MemoryUtil.memUTF8(nameBuffer, length[0])
            }
        }
    }

    fun getUniformBlockOffsets(program: Int, uniformBlockIndex: Int, indices: IntArray): IntArray {
        val offsets = IntArray(indices.size)
        glGetActiveUniformsiv(program, indices, GL_UNIFORM_OFFSET, offsets)
        return offsets
    }

    fun initGLContext(name: String = "") {
        fun createHiddenWindow(): Long {
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
            return glfwCreateWindow(1, 1, name, 0, minecraft.window.handle)
        }

        // glfwCreateWindow must only be called from the main thread.
        val window = minecraft.submit(::createHiddenWindow).get()
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
    }
}