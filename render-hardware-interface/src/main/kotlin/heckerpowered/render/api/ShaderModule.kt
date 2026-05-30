package heckerpowered.render.api

/**
 * Represents a backend-created shader stage object.
 *
 * A shader module is a compiled or otherwise prepared shader stage that can be
 * referenced by pipeline descriptions. The concrete backend decides how shader
 * source or bytecode is transformed into this object.
 *
 * A shader module represents exactly one shader stage.
 */
interface ShaderModule {
    /**
     * Identifies which shader stage this module represents.
     */
    val stage: ShaderStage
}