package heckerpowered.render.api

/**
 * Identifies the storage format of a GPU texture.
 *
 * A texture format describes how texel data is stored and interpreted by the
 * backend. It is a resource property rather than a high-level usage role.
 */
enum class TextureFormat {

    /**
     * Four-channel 8-bit unsigned normalized color format.
     */
    Rgba8UnsignedNormalized,

    /**
     * Four-channel 16-bit floating-point color format.
     */
    Rgba16Float,

    /**
     * Packed depth-stencil format with 24-bit depth and 8-bit stencil.
     */
    Depth24Stencil8,

    /**
     * 32-bit floating-point depth format.
     */
    Depth32Float
}