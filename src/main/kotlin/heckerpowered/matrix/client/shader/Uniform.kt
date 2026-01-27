/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.shader

abstract class Uniform<T>(
    private val uniformName: String,
    private val programObjectId: Int,
    protected val uniformWriter: UniformWriter,
) {
    private var location: UniformLocation? = null
    var value: T? = null
    private var provider: (() -> T)? = null
    private var dirty: Boolean = false

    protected open fun valuesEqual(a: T, b: T): Boolean = a == b
    protected abstract fun write(target: UniformLocation, value: T)

    fun sync() {
        flush(false)
    }

    fun flush(force: Boolean = false) {
        val desired = resolveDesiredValue() ?: return
        val target = resolveLocation() ?: return

        if (!force && !dirty) return

        write(target, desired)
    }

    private fun resolveDesiredValue(): T? {
        // Provider takes precedence; value is fallback.
        val newValue: T? = provider?.invoke() ?: value

        // Compare with previous cached value.
        val previous = value
        if (previous == null && newValue != null ||
            previous != null && newValue == null ||
            previous != null && newValue != null && !valuesEqual(previous, newValue)
        ) {
            dirty = true
        }

        return newValue
    }

    private fun resolveLocation(): UniformLocation? {
        val existing = location
        if (existing != null) return existing

        val resolved = uniformWriter.findLocation(programObjectId, uniformName) ?: return null
        location = resolved
        return resolved
    }
}