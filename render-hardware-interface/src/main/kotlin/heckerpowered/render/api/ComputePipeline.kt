package heckerpowered.render.api

/**
 * Represents a compiled compute pipeline object.
 *
 * A compute pipeline encapsulates the shader program and any fixed pipeline-side
 * state required to execute compute work.
 *
 * The concrete backend decides how the pipeline is created and stored.
 */
interface ComputePipeline