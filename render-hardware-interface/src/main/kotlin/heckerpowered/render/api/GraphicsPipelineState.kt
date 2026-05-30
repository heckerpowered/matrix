package heckerpowered.render.api

/**
 * Represents an ordered sequence of graphics pipeline state commands.
 *
 * The contained commands are interpreted in order. When multiple commands
 * affect the same aspect of pipeline state, later commands override earlier
 * ones.
 *
 * This type stores the declared state sequence rather than a backend-specific
 * compiled result.
 */
data class GraphicsPipelineState(
    val commands: List<PipelineState>
)