/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

val projectionMatrixProvider = UniformProvider("projectionMatrix")
val modelViewMatrixProvider = UniformProvider("modelViewMatrix")
val inverseProjectionMatrixProvider = UniformProvider("inverseProjectionMatrix")
val inverseModelViewMatrixProvider = UniformProvider("inverseModelViewMatrix")
val inverseViewMatrixProvider = UniformProvider("inverseViewMatrix")
val viewMatrixProvider = UniformProvider("viewMatrix")
val viewProjectionMatrixProvider = UniformProvider("viewProjectionMatrix")
val playerPositionProvider = UniformProvider("playerPosition")
val cameraPositionProvider = UniformProvider("cameraPosition")
val resolutionProvider = UniformProvider("resolution")
val timeProvider = UniformProvider("time")

open class UniformProvider(val name: String, val set: (pointer: Int) -> Unit = {}) {
    var pointer = -1

    fun init(program: Int) {
        pointer = -1
    }
}
