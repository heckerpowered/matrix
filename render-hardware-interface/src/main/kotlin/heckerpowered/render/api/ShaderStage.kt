package heckerpowered.render.api

/**
 * Identifies a shader stage.
 *
 * Shader stages are used to classify shader modules and to validate whether a
 * given shader module may be used in a particular pipeline description.
 */
enum class ShaderStage {
    /**
     * Vertex processing stage.
     */
    Vertex,

    /**
     * Tessellation control stage.
     */
    TessellationControl,

    /**
     * Tessellation evaluation stage.
     */
    TessellationEvaluation,

    /**
     * Geometry processing stage.
     */
    Geometry,

    /**
     * Fragment processing stage.
     */
    Fragment,

    /**
     * Compute processing stage.
     */
    Compute
}