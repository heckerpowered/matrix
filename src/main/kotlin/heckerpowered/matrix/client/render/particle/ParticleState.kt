package heckerpowered.matrix.client.render.particle

import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.nio.FloatBuffer

open class ParticleState(val data: FloatBuffer) {
    companion object {
        const val X_INDEX = 0
        const val Y_INDEX = 1
        const val Z_INDEX = 2
        const val POSITION_INDEX = X_INDEX

        const val VELOCITY_X_INDEX = 3
        const val VELOCITY_Y_INDEX = 4
        const val VELOCITY_Z_INDEX = 5
        const val VELOCITY_INDEX = VELOCITY_X_INDEX

        const val ACCELERATION_X_INDEX = 6
        const val ACCELERATION_Y_INDEX = 7
        const val ACCELERATION_Z_INDEX = 8
        const val ACCELERATION_INDEX = ACCELERATION_X_INDEX

        const val SPRITE_SIZE_INDEX = 9
        const val SCALE_X_INDEX = 10

        const val AGE_INDEX = 11
        const val LIFETIME_INDEX = 12

        const val COLOR_R_INDEX = 13
        const val COLOR_G_INDEX = 14
        const val COLOR_B_INDEX = 15
        const val COLOR_A_INDEX = 16
        const val COLOR_INDEX = COLOR_R_INDEX

        const val QUATERNION_X_INDEX = 17
        const val QUATERNION_Y_INDEX = 18
        const val QUATERNION_Z_INDEX = 19
        const val QUATERNION_W_INDEX = 20
        const val QUATERNION_INDEX = QUATERNION_X_INDEX

        const val ANGULAR_VELOCITY_X_INDEX = 21
        const val ANGULAR_VELOCITY_Y_INDEX = 22
        const val ANGULAR_VELOCITY_Z_INDEX = 23
        const val ANGULAR_VELOCITY_INDEX = ANGULAR_VELOCITY_X_INDEX

        const val BYTES = 24 * Float.SIZE_BYTES
    }

    init {
        require(data.capacity() >= 24) { "Particle data's size is too small." }
    }

    var x: Float
        get() = data[X_INDEX]
        set(value) {
            data.put(X_INDEX, value)
        }
    var y: Float
        get() = data[Y_INDEX]
        set(value) {
            data.put(Y_INDEX, value)
        }
    var z: Float
        get() = data[Z_INDEX]
        set(value) {
            data.put(Z_INDEX, value)
        }
    var position: Vector3f
        get() = Vector3f(x, y, z)
        set(value) {
            x = value.x
            y = value.y
            z = value.z
        }

    var velocityX: Float
        get() = data[VELOCITY_X_INDEX]
        set(value) {
            data.put(VELOCITY_X_INDEX, value)
        }
    var velocityY: Float
        get() = data[VELOCITY_Y_INDEX]
        set(value) {
            data.put(VELOCITY_Y_INDEX, value)
        }
    var velocityZ: Float
        get() = data[VELOCITY_Z_INDEX]
        set(value) {
            data.put(VELOCITY_Z_INDEX, value)
        }
    var velocity: Vector3f
        get() = Vector3f(velocityX, velocityY, velocityZ)
        set(value) {
            velocityX = value.x
            velocityY = value.y
            velocityZ = value.z
        }

    var accelerationX: Float
        get() = data[ACCELERATION_X_INDEX]
        set(value) {
            data.put(ACCELERATION_X_INDEX, value)
        }
    var accelerationY: Float
        get() = data[ACCELERATION_Y_INDEX]
        set(value) {
            data.put(ACCELERATION_Y_INDEX, value)
        }
    var accelerationZ: Float
        get() = data[ACCELERATION_Z_INDEX]
        set(value) {
            data.put(ACCELERATION_Z_INDEX, value)
        }
    var acceleration: Vector3f
        get() = Vector3f(accelerationX, accelerationY, accelerationZ)
        set(value) {
            accelerationX = value.x
            accelerationY = value.y
            accelerationZ = value.z
        }

    var spriteSize: Float
        get() = data[SPRITE_SIZE_INDEX]
        set(value) {
            data.put(SPRITE_SIZE_INDEX, value)
        }
    var scale: Float
        get() = data[SCALE_X_INDEX]
        set(value) {
            data.put(SCALE_X_INDEX, value)
        }

    var age: Float
        get() = data[AGE_INDEX]
        set(value) {
            data.put(AGE_INDEX, value)
        }
    var lifetime: Float
        get() = data[LIFETIME_INDEX]
        set(value) {
            data.put(LIFETIME_INDEX, value)
        }

    var colorR: Float
        get() = data[COLOR_R_INDEX]
        set(value) {
            data.put(COLOR_R_INDEX, value)
        }
    var colorG: Float
        get() = data[COLOR_G_INDEX]
        set(value) {
            data.put(COLOR_G_INDEX, value)
        }
    var colorB: Float
        get() = data[COLOR_B_INDEX]
        set(value) {
            data.put(COLOR_B_INDEX, value)
        }
    var colorA: Float
        get() = data[COLOR_A_INDEX]
        set(value) {
            data.put(COLOR_A_INDEX, value)
        }
    var colorRGBA: Vector4f
        get() = Vector4f(colorR, colorG, colorB, colorA)
        set(value) {
            colorR = value.x
            colorG = value.y
            colorB = value.z
            colorA = value.w
        }

    var quaternionX: Float
        get() = data[QUATERNION_X_INDEX]
        set(value) {
            data.put(QUATERNION_X_INDEX, value)
        }
    var quaternionY: Float
        get() = data[QUATERNION_Y_INDEX]
        set(value) {
            data.put(QUATERNION_Y_INDEX, value)
        }
    var quaternionZ: Float
        get() = data[QUATERNION_Z_INDEX]
        set(value) {
            data.put(QUATERNION_Z_INDEX, value)
        }
    var quaternionW: Float
        get() = data[QUATERNION_W_INDEX]
        set(value) {
            data.put(QUATERNION_W_INDEX, value)
        }
    var quaternion: Quaternionf
        get() = Quaternionf(quaternionX, quaternionY, quaternionZ, quaternionW)
        set(value) {
            quaternionX = value.x
            quaternionY = value.y
            quaternionZ = value.z
            quaternionW = value.w
        }

    var angularVelocityX: Float
        get() = data[ANGULAR_VELOCITY_X_INDEX]
        set(value) {
            data.put(ANGULAR_VELOCITY_X_INDEX, value)
        }
    var angularVelocityY: Float
        get() = data[ANGULAR_VELOCITY_Y_INDEX]
        set(value) {
            data.put(ANGULAR_VELOCITY_Y_INDEX, value)
        }
    var angularVelocityZ: Float
        get() = data[ANGULAR_VELOCITY_Z_INDEX]
        set(value) {
            data.put(ANGULAR_VELOCITY_Z_INDEX, value)
        }
    var angularVelocity: Vector3f
        get() = Vector3f(angularVelocityX, angularVelocityY, angularVelocityZ)
        set(value) {
            angularVelocityX = value.x
            angularVelocityY = value.y
            angularVelocityZ = value.z
        }

    override fun equals(other: Any?): Boolean {
        return other is ParticleState && this.data === other.data
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }
}