/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

import kotlinx.coroutines.Dispatchers

object ShaderCompilerV1 {
    val Dispatcher = Dispatchers.Default
    fun compileShader(source: String, type: Int): Int = 0
}
