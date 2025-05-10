package heckerpowered.matrix.client.render.particle

import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil

data class GpuParticleState(
    val vertexArrayObject: Int,
    var vertexBufferObjectPing: Int,
    var vertexBufferObjectPong: Int,
    val particleCount: Int,
) {
    companion object {
        fun createGpuParticleState(particleCount: Int): GpuParticleState {
            val vertexArrayObject = glGenVertexArrays()
            val vertexBufferObjectPing = glGenBuffers()
            val vertexBufferObjectPong = glGenBuffers()

            glBindVertexArray(vertexArrayObject)

            val bufferSize = particleCount * ParticleState.BYTES.toLong()

            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPing)
            glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_DYNAMIC_COPY)

            fun defineAttrib(index: Int, size: Int, offsetIndex: Int) {
                glEnableVertexAttribArray(index)
                glVertexAttribPointer(index, size, GL_FLOAT, false, ParticleState.BYTES, (offsetIndex * Float.SIZE_BYTES).toLong())
            }

            defineAttrib(0, 3, ParticleState.POSITION_INDEX)          // position.xyz
            defineAttrib(1, 3, ParticleState.VELOCITY_INDEX)          // velocity.xyz
            defineAttrib(2, 3, ParticleState.ACCELERATION_INDEX)      // acceleration.xyz
            defineAttrib(3, 1, ParticleState.SPRITE_SIZE_INDEX)       // sprite size
            defineAttrib(4, 1, ParticleState.SCALE_INDEX)             // size scale
            defineAttrib(5, 1, ParticleState.AGE_INDEX)               // age
            defineAttrib(6, 1, ParticleState.LIFETIME_INDEX)          // lifetime
            defineAttrib(7, 4, ParticleState.COLOR_INDEX)             // color.rgba
            defineAttrib(8, 4, ParticleState.ORIENTATION_INDEX)       // orientation quaternion
            defineAttrib(9, 3, ParticleState.ANGULAR_VELOCITY_INDEX)  // angular velocity

            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPong)
            glBufferData(GL_ARRAY_BUFFER, bufferSize, GL_DYNAMIC_COPY)

            glBindVertexArray(0)
            glBindBuffer(GL_ARRAY_BUFFER, 0)

            return GpuParticleState(vertexArrayObject, vertexBufferObjectPing, vertexBufferObjectPong, particleCount)
        }
    }

    fun bind() {
        glBindVertexArray(vertexArrayObject)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectPing)
    }

    fun unbind() {
        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun swapBuffers() {
        val ping = vertexBufferObjectPing
        vertexBufferObjectPing = vertexBufferObjectPong
        vertexBufferObjectPong = ping
    }

    fun retrieve(): ParticleStateDump {
        return ParticleStateDump(this)
    }

    fun delete() {
        glDeleteVertexArrays(vertexArrayObject)
        glDeleteBuffers(vertexBufferObjectPing)
        glDeleteBuffers(vertexBufferObjectPong)
    }

    fun initParticles(initializer: (ParticleState) -> Unit) {
        val buffer = MemoryUtil.memAllocFloat(particleCount * ParticleState.ELEMENTS)
        buffer.clear()
        bind()
        repeat(particleCount) {
            val particleState = ParticleState(buffer.slice(it * ParticleState.ELEMENTS, ParticleState.ELEMENTS))
            initializer(particleState)
        }
        buffer.rewind()
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_COPY)
        unbind()
        MemoryUtil.memFree(buffer)
    }
}