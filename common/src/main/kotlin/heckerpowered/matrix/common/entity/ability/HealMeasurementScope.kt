/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.common.entity.ability

object HealMeasurementScope {
    private val frames = ThreadLocal.withInitial { ArrayDeque<Frame>() }

    class Frame {
        var resolvedAmount: Float = 0.0F
        var restoredHealth: Float = 0.0F
    }

    @JvmStatic
    fun current(): Frame? = frames.get().lastOrNull()

    fun measure(block: () -> Unit): HealMeasurement {
        val frame = Frame()
        val stack = frames.get()
        stack.addLast(frame)

        try {
            block()
            return HealMeasurement(
                resolvedAmount = frame.resolvedAmount,
                restoredHealth = frame.restoredHealth,
            )
        } finally {
            val removed = stack.removeLast()
            check(removed === frame) { "Heal measurement scope is unbalanced." }

            if (stack.isEmpty()) {
                frames.remove()
            }
        }
    }
}