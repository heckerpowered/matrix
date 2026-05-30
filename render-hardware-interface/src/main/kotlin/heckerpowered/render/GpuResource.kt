package heckerpowered.render

/**
 * Represents any GPU-owned resource that can participate in command recording.
 *
 * This interface is the common parent for resources that may require usage
 * transitions or backend-specific synchronization.
 *
 * A GPU resource is identified by its role in the rendering pipeline rather than
 * by any backend handle type.
 */
interface GpuResource

/**
 * Represents a GPU buffer resource.
 *
 * A buffer stores linear byte-addressable data such as:
 * - vertex data
 * - index data
 * - uniform data
 * - storage data
 * - transfer data
 *
 * The concrete backend decides how the buffer is created and bound.
 */
interface GpuBuffer : GpuResource

/**
 * Represents a GPU texture resource.
 *
 * A texture stores formatted image data and may be used for:
 * - sampling in shaders
 * - render pass attachments
 * - transfer operations
 *
 * The concrete backend decides how the texture is created, stored, and bound.
 */
interface GpuTexture : GpuResource

/**
 * Represents a GPU sampler object.
 *
 * A sampler describes how texture sampling should behave, such as filtering
 * and addressing rules.
 *
 * A sampler is not treated as a general GPU resource because it does not
 * participate in usage transitions in the same way as buffers and textures.
 */
interface GpuSampler