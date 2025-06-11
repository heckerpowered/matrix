package heckerpowered.matrix.core.extensions

import org.joml.Matrix4f
import org.joml.Quaternionf

object Matrix4fExtensions {
    operator fun Matrix4f.times(rotation: Quaternionf): Matrix4f {
        val result = Matrix4f()
        rotate(rotation, result)
        return result
    }

    operator fun Matrix4f.timesAssign(rotation: Quaternionf) {
        rotate(rotation)
    }
}