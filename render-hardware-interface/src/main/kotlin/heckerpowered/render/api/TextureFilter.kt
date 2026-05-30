package heckerpowered.render.api

/**
 * Identifies a basic texture sampling filter.
 *
 * Texture filters control how sampled texel values are selected or
 * interpolated when accessing a texture through a sampler.
 */
enum class TextureFilter {

    /**
     * Selects the nearest texel without interpolation.
     */
    Nearest,

    /**
     * Interpolates between neighboring texels.
     */
    Linear
}