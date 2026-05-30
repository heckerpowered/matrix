package heckerpowered.render.api

/**
 * Describes a GPU texture before it is created by a device.
 *
 * A texture description defines the dimensions and storage format of the
 * texture. It does not define how the texture will be used by a particular
 * render pass or shader binding site.
 *
 * This type is pure description data. It does not contain backend state.
 */
data class TextureDescription(
    val widthPixels: Int,
    val heightPixels: Int,
    val format: TextureFormat
)