package heckerpowered.render.api

/**
 * Enables or disables depth testing.
 *
 * When multiple depth test enable commands appear in the same graphics pipeline
 * state sequence, later commands override earlier ones.
 */
data class DepthTestState(
    val enabled: Boolean
) : PipelineState