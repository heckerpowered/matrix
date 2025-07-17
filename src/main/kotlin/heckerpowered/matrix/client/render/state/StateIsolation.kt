package heckerpowered.matrix.client.render.state

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
object StateIsolation {
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