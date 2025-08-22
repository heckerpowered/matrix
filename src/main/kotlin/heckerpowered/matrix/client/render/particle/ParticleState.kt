/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.render.particle

import heckerpowered.matrix.client.render.particle.memory.MemoryLayout
import heckerpowered.matrix.client.render.particle.module.ParticleStateElement.*
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.nio.FloatBuffer

open class ParticleState(val data: FloatBuffer, val layout: MemoryLayout) {
    init {
        require(data.capacity() >= layout.floats) { "${layout.name} requires at least ${layout.floats} floats, but only ${data.capacity()} are available" }
    }

    var x: Float
        get() = data[layout[POSITION_X]]
        set(value) {
            data.put(layout[POSITION_X], value)
        }
    var y: Float
        get() = data[layout[POSITION_Y]]
        set(value) {
            data.put(layout[POSITION_Y], value)
        }
    var z: Float
        get() = data[layout[POSITION_Z]]
        set(value) {
            data.put(layout[POSITION_Z], value)
        }
    var position: Vector3f
        get() = Vector3f(x, y, z)
        set(value) {
            x = value.x
            y = value.y
            z = value.z
        }
    val positionBuffer: FloatBuffer
        get() = data.slice(layout[POSITION_X], 3)

    var velocityX: Float
        get() = data[layout[VELOCITY_X]]
        set(value) {
            data.put(layout[VELOCITY_X], value)
        }
    var velocityY: Float
        get() = data[layout[VELOCITY_Y]]
        set(value) {
            data.put(layout[VELOCITY_Y], value)
        }
    var velocityZ: Float
        get() = data[layout[VELOCITY_Z]]
        set(value) {
            data.put(layout[VELOCITY_Z], value)
        }
    var velocity: Vector3f
        get() = Vector3f(velocityX, velocityY, velocityZ)
        set(value) {
            velocityX = value.x
            velocityY = value.y
            velocityZ = value.z
        }
    val velocityBuffer: FloatBuffer
        get() = data.slice(layout[VELOCITY_X], 3)

    var accelerationX: Float
        get() = data[layout[ACCELERATION_X]]
        set(value) {
            data.put(layout[ACCELERATION_X], value)
        }
    var accelerationY: Float
        get() = data[layout[ACCELERATION_Y]]
        set(value) {
            data.put(layout[ACCELERATION_Y], value)
        }
    var accelerationZ: Float
        get() = data[layout[ACCELERATION_Z]]
        set(value) {
            data.put(layout[ACCELERATION_Z], value)
        }
    var acceleration: Vector3f
        get() = Vector3f(accelerationX, accelerationY, accelerationZ)
        set(value) {
            accelerationX = value.x
            accelerationY = value.y
            accelerationZ = value.z
        }
    val accelerationBuffer: FloatBuffer
        get() = data.slice(layout[ACCELERATION_X], 3)

    var spriteSize: Float
        get() = data[layout[SPRITE_SIZE]]
        set(value) {
            data.put(layout[SPRITE_SIZE], value)
        }
    var scale: Float
        get() = data[layout[SCALE]]
        set(value) {
            data.put(layout[SCALE], value)
        }

    var age: Float
        get() = data[layout[AGE]]
        set(value) {
            data.put(layout[AGE], value)
        }
    var lifetime: Float
        get() = data[layout[LIFETIME]]
        set(value) {
            data.put(layout[LIFETIME], value)
        }

    var colorR: Float
        get() = data[layout[COLOR_R]]
        set(value) {
            data.put(layout[COLOR_R], value)
        }
    var colorG: Float
        get() = data[layout[COLOR_G]]
        set(value) {
            data.put(layout[COLOR_G], value)
        }
    var colorB: Float
        get() = data[layout[COLOR_B]]
        set(value) {
            data.put(layout[COLOR_B], value)
        }
    var colorA: Float
        get() = data[layout[COLOR_A]]
        set(value) {
            data.put(layout[COLOR_A], value)
        }
    var colorRGBA: Vector4f
        get() = Vector4f(colorR, colorG, colorB, colorA)
        set(value) {
            colorR = value.x
            colorG = value.y
            colorB = value.z
            colorA = value.w
        }
    val colorBuffer: FloatBuffer
        get() = data.slice(layout[COLOR_R], 4)

    var orientationX: Float
        get() = data[layout[ORIENTATION_X]]
        set(value) {
            data.put(layout[ORIENTATION_X], value)
        }
    var orientationY: Float
        get() = data[layout[ORIENTATION_Y]]
        set(value) {
            data.put(layout[ORIENTATION_Y], value)
        }
    var orientationZ: Float
        get() = data[layout[ORIENTATION_Z]]
        set(value) {
            data.put(layout[ORIENTATION_Z], value)
        }
    var orientationW: Float
        get() = data[layout[ORIENTATION_W]]
        set(value) {
            data.put(layout[ORIENTATION_W], value)
        }
    var orientation: Quaternionf
        get() = Quaternionf(orientationX, orientationY, orientationZ, orientationW)
        set(value) {
            orientationX = value.x
            orientationY = value.y
            orientationZ = value.z
            orientationW = value.w
        }
    val orientationBuffer: FloatBuffer
        get() = data.slice(layout[ORIENTATION_X], 4)

    var angularVelocityX: Float
        get() = data[layout[ANGULAR_VELOCITY_X]]
        set(value) {
            data.put(layout[ANGULAR_VELOCITY_X], value)
        }
    var angularVelocityY: Float
        get() = data[layout[ANGULAR_VELOCITY_Y]]
        set(value) {
            data.put(layout[ANGULAR_VELOCITY_Y], value)
        }
    var angularVelocityZ: Float
        get() = data[layout[ANGULAR_VELOCITY_Z]]
        set(value) {
            data.put(layout[ANGULAR_VELOCITY_Z], value)
        }
    var angularVelocity: Vector3f
        get() = Vector3f(angularVelocityX, angularVelocityY, angularVelocityZ)
        set(value) {
            angularVelocityX = value.x
            angularVelocityY = value.y
            angularVelocityZ = value.z
        }
    val angularVelocityBuffer: FloatBuffer
        get() = data.slice(layout[ANGULAR_VELOCITY_X], 3)

    override fun equals(other: Any?): Boolean {
        return other is ParticleState && this.data === other.data
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    override fun toString(): String {
        return "ParticleState(data=$data, x=$x, y=$y, z=$z, position=$position, positionBuffer=$positionBuffer, velocityX=$velocityX, velocityY=$velocityY, velocityZ=$velocityZ, velocity=$velocity, velocityBuffer=$velocityBuffer, accelerationX=$accelerationX, accelerationY=$accelerationY, accelerationZ=$accelerationZ, acceleration=$acceleration, accelerationBuffer=$accelerationBuffer, spriteSize=$spriteSize, scale=$scale, age=$age, lifetime=$lifetime, colorR=$colorR, colorG=$colorG, colorB=$colorB, colorA=$colorA, colorRGBA=$colorRGBA, colorBuffer=$colorBuffer, orientationX=$orientationX, orientationY=$orientationY, orientationZ=$orientationZ, orientationW=$orientationW, orientation=$orientation, orientationBuffer=$orientationBuffer, angularVelocityX=$angularVelocityX, angularVelocityY=$angularVelocityY, angularVelocityZ=$angularVelocityZ, angularVelocity=$angularVelocity, angularVelocityBuffer=$angularVelocityBuffer)"
    }
}