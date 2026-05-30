package heckerpowered.render.api

import heckerpowered.render.GpuBuffer
import heckerpowered.render.GpuSampler
import heckerpowered.render.GpuTexture

/**
 * Represents a concrete resource binding set.
 *
 * A binding set supplies actual GPU resources for a previously defined binding
 * layout. The meaning of each bound resource is determined by the corresponding
 * binding declarations in [layout] and by the pipeline that consumes the set.
 */
interface BindingSet {
    /**
     * The binding layout that defines the structure of this binding set.
     */
    val layout: BindingLayout

    /**
     * Binds a buffer resource to the specified binding index.
     *
     * @param binding The binding index.
     * @param buffer The buffer resource to bind.
     */
    fun writeBuffer(binding: Int, buffer: GpuBuffer)

    /**
     * Binds a texture resource to the specified binding index.
     *
     * @param binding The binding index.
     * @param texture The texture resource to bind.
     * @param sampler The sampler used when accessing the texture. This may be null
     * when the binding model or backend does not require a separate sampler object.
     */
    fun writeTexture(
        binding: Int,
        texture: GpuTexture,
        sampler: GpuSampler? = null
    )
}