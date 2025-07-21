package heckerpowered.matrix.client.render.particle.memory

import com.mojang.blaze3d.systems.RenderSystem
import heckerpowered.matrix.client.render.particle.module.ParticleStateElement
import heckerpowered.matrix.client.render.particle.module.ParticleStateElement.*
import org.lwjgl.opengl.GL46.*

class Std140Layout : MemoryLayout() {
    companion object {
        val sharedUniformBufferObject by lazy {
            RenderSystem.assertOnRenderThread()

            val uniformBufferObject = glGenBuffers()

            glBindBuffer(GL_UNIFORM_BUFFER, uniformBufferObject)
            glBufferData(GL_UNIFORM_BUFFER, STD_140.bufferSizeBytes.toLong(), GL_DYNAMIC_DRAW)
            glBindBuffer(GL_UNIFORM_BUFFER, 0)

            return@lazy uniformBufferObject
        }
    }

    override val bufferSizeBytes: Int
        get() = floats * Float.SIZE_BYTES
    override val floats: Int
        get() = 24 + 4 // 4 floats for padding

    override fun getPosition(element: ParticleStateElement) = when (element) {
        // Offset of Position = 0
        POSITION_X -> 0
        POSITION_Y -> 1
        POSITION_Z -> 2

        // Offset of Velocity = 16
        VELOCITY_X -> 4
        VELOCITY_Y -> 5
        VELOCITY_Z -> 6

        // Offset of Acceleration = 32
        ACCELERATION_X -> 8
        ACCELERATION_Y -> 9
        ACCELERATION_Z -> 10

        SPRITE_SIZE -> 11 // Offset of SpriteSize = 44
        SCALE -> 12 // Offset of Scale = 48
        AGE -> 13 // Offset of Age = 52
        LIFETIME -> 14 // Offset of Lifetime = 56

        // Offset of Color = 64
        COLOR_R -> 16
        COLOR_G -> 17
        COLOR_B -> 18
        COLOR_A -> 19

        // Offset of Orientation = 80
        ORIENTATION_X -> 20
        ORIENTATION_Y -> 21
        ORIENTATION_Z -> 22
        ORIENTATION_W -> 23

        // Offset of Angular Velocity = 96
        ANGULAR_VELOCITY_X -> 24
        ANGULAR_VELOCITY_Y -> 25
        ANGULAR_VELOCITY_Z -> 26
    }

    override val sharedUniformBufferObject: Int
        get() = Std140Layout.sharedUniformBufferObject
}