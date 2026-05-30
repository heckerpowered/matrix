package heckerpowered.render.api

/**
 * Identifies the intended usage of a GPU buffer.
 *
 * Buffer usage communicates the primary role of a buffer to the backend and to
 * higher-level validation logic.
 */
enum class BufferUsage {

    /**
     * The buffer is intended to be used as a vertex buffer.
     */
    Vertex,

    /**
     * The buffer is intended to be used as an index buffer.
     */
    Index,

    /**
     * The buffer is intended to be used as a uniform buffer.
     */
    Uniform,

    /**
     * The buffer is intended to be used as a storage buffer.
     */
    Storage,

    /**
     * The buffer is intended to be used as a transfer source.
     */
    TransferSource,

    /**
     * The buffer is intended to be used as a transfer destination.
     */
    TransferDestination
}