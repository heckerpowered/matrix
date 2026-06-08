/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import org.joml.Vector3f

object PositionColorProgram : Program() {
    var color = Vector3f(1.0F, 1.0F, 1.0F)
}
