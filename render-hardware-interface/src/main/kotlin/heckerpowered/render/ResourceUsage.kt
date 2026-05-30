package heckerpowered.render

/**
 * Describes the intended next usage of a GPU resource.
 *
 * This value expresses high-level usage intent rather than backend-specific
 * synchronization details. Backends may translate it into pipeline barriers,
 * state transitions, layout changes, or no operation if no explicit action is needed.
 *
 * The value does not describe the permanent nature of a resource. The same resource
 * may be used with different usages across different passes within the same frame.
 */
enum class ResourceUsage {
    /**
     * The resource will be read as a vertex buffer.
     */
    VertexRead,

    /**
     * The resource will be read as an index buffer.
     */
    IndexRead,

    /**
     * The resource will be read as a uniform buffer.
     */
    UniformRead,

    /**
     * The resource will be read as a storage resource.
     *
     * This typically applies to shader storage buffers or storage images
     * when they are accessed in read-only form.
     */
    StorageRead,

    /**
     * The resource will be written as a storage resource.
     *
     * This typically applies to shader storage buffers or storage images
     * when they are written by a shader stage.
     */
    StorageWrite,

    /**
     * The resource will be read through texture sampling.
     */
    SampledRead,

    /**
     * The resource will be written as a color attachment in a render pass.
     */
    ColorWrite,

    /**
     * The resource will be written as a depth attachment in a render pass.
     */
    DepthWrite,

    /**
     * The resource will be used as a transfer source.
     */
    TransferSource,

    /**
     * The resource will be used as a transfer destination.
     */
    TransferDestination
}