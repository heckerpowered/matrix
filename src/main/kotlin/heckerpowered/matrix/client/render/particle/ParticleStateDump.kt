package heckerpowered.matrix.client.render.particle

import org.lwjgl.opengl.GL11.glFinish
import org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER
import org.lwjgl.opengl.GL15.glGetBufferSubData
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class ParticleStateDump(val particleStates: GpuParticleState) : AutoCloseable {
    val buffer: FloatBuffer = MemoryUtil.memAllocFloat(particleStates.particleCount * ParticleState.ELEMENTS)
    val particles: Array<ParticleState> = run {
        buffer.clear()
        println("Dump from: ${particleStates.vertexBufferObjectPing}")
        particleStates.bind()
        glFinish()
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffer)
        particleStates.unbind()

        buffer.rewind()
        Array(particleStates.particleCount) {
            ParticleState(buffer.slice(it * ParticleState.ELEMENTS, ParticleState.ELEMENTS))
        }
    }

    fun refresh() {
        buffer.clear()
        particleStates.bind()
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, buffer)
        particleStates.unbind()
    }

    override fun close() {
        MemoryUtil.memFree(buffer)
    }
}