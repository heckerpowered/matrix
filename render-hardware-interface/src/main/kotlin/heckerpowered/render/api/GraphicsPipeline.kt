package heckerpowered.render.api

/**
 * Represents a compiled graphics pipeline object.
 *
 * A graphics pipeline encapsulates the shader program and any fixed pipeline-side
 * state required to issue draw commands.
 *
 * The concrete backend decides how the pipeline is created and stored.
 */
interface GraphicsPipeline