package heckerpowered.render.api

/**
 * Describes a GPU sampler before it is created by a device.
 *
 * A sampler description defines how texture sampling should behave. This type
 * is pure description data. It does not contain backend state.
 */
data class SamplerDescription(
    val minificationFilter: TextureFilter,
    val magnificationFilter: TextureFilter
)