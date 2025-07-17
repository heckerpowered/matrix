package heckerpowered.matrix.client.render.state

/**
 * Represents a state that can be applied to the current rendering pipeline.
 *
 * @author heckerpowered
 */
fun interface RenderPipelineState {
    /**
     * Applies the state to the current rendering pipeline and saves the previously active state as a snapshot.
     *
     * @return a [RenderPipelineSnapshot] representing the saved state prior to applying this one,
     *         allowing restoration after the state is no longer needed.
     */
    fun apply(): RenderPipelineSnapshot
}