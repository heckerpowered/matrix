package heckerpowered.matrix.client.render.particle

import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL46.*
import org.lwjgl.system.MemoryUtil

data class GpuParticleState(
    val vertexArrayObject: Int,
    val vertexBufferObject: Int,
    val particleCount: Int,
) {
    companion object {
        fun createGpuParticleState(particleCount: Int): GpuParticleState {
            val vertexArrayObject = glGenVertexArrays()
            val vertexBufferObject = glGenBuffers()

            glBindVertexArray(vertexArrayObject)
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject)

            val buffer = MemoryUtil.memAllocFloat(particleCount).flip()
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW)
            MemoryUtil.memFree(buffer)

            val stride = particleCount * ParticleState.BYTES
            fun defineAttrib(index: Int, size: Int, offsetIndex: Int) {
                glEnableVertexAttribArray(index)
                glVertexAttribPointer(
                    index, size, GL_FLOAT, false, stride, (offsetIndex * Float.SIZE_BYTES).toLong()
                )
            }

            defineAttrib(0, 3, ParticleState.POSITION_INDEX)          // position.xyz
            defineAttrib(1, 3, ParticleState.VELOCITY_INDEX)          // velocity.xyz
            defineAttrib(2, 3, ParticleState.ACCELERATION_INDEX)      // acceleration.xyz
            defineAttrib(3, 4, ParticleState.COLOR_INDEX)             // color.rgba
            defineAttrib(4, 4, ParticleState.QUATERNION_INDEX)        // rotation quaternion
            defineAttrib(5, 3, ParticleState.ANGULAR_VELOCITY_INDEX)  // angular velocity
            defineAttrib(6, 1, ParticleState.AGE_INDEX)               // age
            defineAttrib(7, 1, ParticleState.LIFETIME_INDEX)          // lifetime
            defineAttrib(8, 2, ParticleState.SPRITE_SIZE_INDEX)       // sprite size + scale

            glBindVertexArray(0)
            glBindBuffer(GL_ARRAY_BUFFER, 0)

            return GpuParticleState(vertexArrayObject, vertexBufferObject, particleCount)
        }
    }

    fun delete() {
        glDeleteVertexArrays(vertexArrayObject)
        glDeleteBuffers(vertexBufferObject)
    }
}