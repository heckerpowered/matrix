/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.shader.component

abstract class ShaderComponent {
    var program = -1
    var enabled = true

    open fun init(program: Int) {
        this.program = program
    }

    abstract fun enable()
    abstract fun disable()
    open fun delete() {}
}