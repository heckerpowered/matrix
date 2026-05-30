package heckerpowered.render.api

/**
 * Identifies the kind of resource expected by a binding declaration.
 *
 * A binding type describes the resource category that may be bound at a given
 * binding index. It is part of the binding layout contract and is used to
 * validate concrete binding set contents against shader and pipeline
 * expectations.
 */
enum class BindingType {
    /**
     * The binding expects a uniform buffer resource.
     */
    UniformBuffer,

    /**
     * The binding expects a storage buffer resource.
     */
    StorageBuffer,

    /**
     * The binding expects a sampled texture resource.
     */
    SampledTexture
}