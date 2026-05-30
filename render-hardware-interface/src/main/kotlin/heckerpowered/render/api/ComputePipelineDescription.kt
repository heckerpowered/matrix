package heckerpowered.render.api

/**
 * Describes a compute pipeline before it is created by a device.
 *
 * A compute pipeline description contains the shader stage and fixed pipeline
 * information required to create a compute pipeline object.
 *
 * This type is pure description data. It does not contain backend state.
 */
data class ComputePipelineDescription(
    val computeShader: ShaderModule
)