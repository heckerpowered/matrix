package heckerpowered.matrix.client.shader.component

import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glDeleteBuffers
import org.lwjgl.opengl.GL30.glTransformFeedbackVaryings
import org.lwjgl.opengl.GL40.glDeleteTransformFeedbacks
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil

class TransformFeedback(
    private val varyingNames: Array<String> = emptyArray(),
    private val vertexCount: Int = 0,
    private val drawMode: Int = GL_POINTS,
    // vec4 by default, 4 float for each varying, 4 byte for each float
    private val bufferSize: Long = vertexCount * 4 * 4 * varyingNames.size.toLong(),
    private val initOnly: Boolean = false,
) : ShaderComponent() {
    private val transformFeedbacks = glGenTransformFeedbacks()
    val buffer = glGenBuffers()

    override fun init(program: Int) {
        super.init(program)
        enabled = !initOnly

        glTransformFeedbackVaryings(program, varyingNames, GL_INTERLEAVED_ATTRIBS)
        if (initOnly) {
            return
        }

        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_STATIC_READ)
    }

    override fun enable() {
        glEnable(GL_RASTERIZER_DISCARD)

        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, transformFeedbacks)
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, buffer)

        glBeginTransformFeedback(drawMode)
    }

    override fun disable() {
        glEndTransformFeedback()
        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0)
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0)

        glDisable(GL_RASTERIZER_DISCARD)
        glBindVertexArray(0)
    }

    override fun delete() {
        glDeleteTransformFeedbacks(transformFeedbacks)
        glDeleteBuffers(buffer)
    }

    fun readBuffer(): FloatArray {
        glBindBuffer(GL_ARRAY_BUFFER, buffer)
        val floatCount = bufferSize.toInt() / 4
        val floatBuffer = MemoryUtil.memAllocFloat(floatCount)
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, floatBuffer)
        val floatArray = FloatArray(floatCount) {
            floatBuffer.get(it)
        }
        MemoryUtil.memFree(floatBuffer)
        return floatArray
    }
}