package heckerpowered.render.api

/**
 * Enables or disables blending.
 *
 * When multiple blend enable commands appear in the same graphics pipeline
 * state sequence, later commands override earlier ones.
 */
data class BlendState(
    val enabled: Boolean
) : PipelineState