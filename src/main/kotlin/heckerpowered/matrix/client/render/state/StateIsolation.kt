/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.render.state

import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * Utility object for isolating changes to render pipeline states.
 *
 * Provides a safe way to temporarily apply a group of [RenderPipelineState] instances,
 * execute some rendering logic, and automatically restore the previous states afterward.
 *
 * Typical usage:
 * ```
 * StateIsolation.isolate(
 *     FramebufferState(0),
 *     ViewportState(0, 0, 100, 100)
 * ) {
 *     // Rendering logic
 * }
 * ```
 *
 * @author heckerpowered
 */
class StateIsolation(
    states: List<RenderPipelineState> = emptyList(),
) : AutoCloseable {
    private val snapshots: MutableList<RenderPipelineSnapshot> = mutableListOf()

    companion object {
        val MARKER: Marker = MarkerFactory.getMarker("STATE_ISOLATION")

        /**
         * Applies the given render pipeline states to the current rendering context,
         * executes the specified operation, and then restores all previously modified states.
         *
         * States are applied in the order they are provided, and restored in reverse order.
         * The restoration are guaranteed to be executed even if [operation] throws an exception,
         * does not guarantee restoration of any states that are modified within the [operation].
         *
         * @param states the render pipeline states to apply.
         * @param operation the operation to execute while the specified states are active.
         */
        fun isolate(vararg states: RenderPipelineState, operation: () -> Unit) {
            // Apply each state and collect snapshots so they can be restored later.
            val snapshots = states.map { it.apply() } // Note: apply() may have side effects
            try {
                operation()
            } finally {
                // Restore the previous states in reverse order (last-applied is restored first).
                snapshots.asReversed().forEach { it.restore() }
            }
        }
    }

    init {
        push(states)
    }

    /**
     * Applies one or more render pipeline states immediately and records their snapshots
     * so they can be restored later (in reverse order) when this isolation scope is closed.
     *
     * Each provided state’s `apply()` method will be invoked, and the resulting
     * `RenderPipelineSnapshot` will be added to this instance’s snapshot stack.
     *
     * @param states one or more [RenderPipelineState] instances to apply; their
     *               snapshots will be stored for subsequent restoration.
     */
    fun push(vararg states: RenderPipelineState) {
        val snapshots = states.map { it.apply() }
        this.snapshots.addAll(snapshots.toTypedArray())
    }

    /**
     * Applies one or more render pipeline states immediately and records their snapshots
     * so they can be restored later (in reverse order) when this isolation scope is closed.
     *
     * Each provided state’s `apply()` method will be invoked, and the resulting
     * `RenderPipelineSnapshot` will be added to this instance’s snapshot stack.
     *
     * @param states one or more [RenderPipelineState] instances to apply; their
     *               snapshots will be stored for subsequent restoration.
     */
    fun push(states: Collection<RenderPipelineState>) {
        val snapshots = states.map { it.apply() }
        this.snapshots.addAll(snapshots.toTypedArray())
    }

    override fun close() {
        snapshots.asReversed().forEach { it.restore() }
    }
}