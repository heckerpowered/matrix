package heckerpowered.render.api

/**
 * Describes a GPU buffer before it is created by a device.
 *
 * A buffer description defines the size and intended usage of the buffer.
 * This type is pure description data. It does not contain backend state.
 */
data class BufferDescription(
    val sizeBytes: Long,
    val usage: BufferUsage
)