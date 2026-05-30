package heckerpowered.render.api

import heckerpowered.render.GpuBuffer
import heckerpowered.render.GpuSampler
import heckerpowered.render.GpuTexture

/**
 * Represents the primary entry point for GPU object creation and frame recording.
 *
 * A graphics device creates long-lived GPU objects such as buffers, textures,
 * samplers, pipelines, and binding objects.
 *
 * A graphics device may also begin frame recording. The resulting frame
 * recorder owns the lifecycle of that frame recording process.
 *
 * The concrete backend decides how these operations are implemented.
 */
interface GraphicsDevice {

    /**
     * Begins recording a new frame.
     *
     * The returned frame recorder owns the lifecycle of the frame recording and
     * provides access to the frame scope used by ordinary rendering systems.
     *
     * @return A frame recorder for the new frame.
     */
    fun beginFrameRecording(): FrameRecorder

    /**
     * Creates a GPU buffer.
     *
     * @param description The buffer description.
     * @return The created GPU buffer.
     */
    fun createBuffer(description: BufferDescription): GpuBuffer

    /**
     * Creates a GPU texture.
     *
     * @param description The texture description.
     * @return The created GPU texture.
     */
    fun createTexture(description: TextureDescription): GpuTexture

    /**
     * Creates a GPU sampler.
     *
     * @param description The sampler description.
     * @return The created GPU sampler.
     */
    fun createSampler(description: SamplerDescription): GpuSampler

    /**
     * Creates a graphics pipeline.
     *
     * @param description The graphics pipeline description.
     * @return The created graphics pipeline.
     */
    fun createGraphicsPipeline(description: GraphicsPipelineDescription): GraphicsPipeline

    /**
     * Creates a compute pipeline.
     *
     * @param description The compute pipeline description.
     * @return The created compute pipeline.
     */
    fun createComputePipeline(description: ComputePipelineDescription): ComputePipeline

    /**
     * Creates a resource binding layout.
     *
     * @param description The binding layout description.
     * @return The created binding layout.
     */
    fun createBindingLayout(description: BindingLayoutDescription): BindingLayout

    /**
     * Creates a concrete binding set for the given layout.
     *
     * @param layout The layout that defines the structure of the binding set.
     * @return The created binding set.
     */
    fun createBindingSet(layout: BindingLayout): BindingSet
}