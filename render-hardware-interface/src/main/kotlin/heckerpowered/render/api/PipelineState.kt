package heckerpowered.render.api

/**
 * Represents a single graphics pipeline state command.
 *
 * Pipeline state commands are interpreted in order. When multiple commands
 * affect the same aspect of pipeline state, later commands override earlier
 * ones.
 *
 * Concrete pipeline state types describe individual state changes such as
 * blending, depth testing, rasterization, or other fixed-function behavior.
 */
sealed interface PipelineState