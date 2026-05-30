package heckerpowered.render.api

import heckerpowered.render.GpuBuffer

/**
 * Exposes commands that may be recorded within a render pass scope.
 *
 * A render pass scope is valid only for the duration of the block passed to
 * CommandRecorder.renderPass. It is not responsible for pass lifetime
 * management. The recorder implementation opens and closes the underlying pass.
 */
interface RenderPassScope {
    /**
     * Binds the graphics pipeline used by subsequent draw commands in this scope.
     *
     * @param pipeline The graphics pipeline to bind.
     */
    fun bindPipeline(pipeline: GraphicsPipeline)

    /**
     * Binds a resource set used by the currently bound graphics pipeline.
     *
     * The meaning of each bound resource is determined by the binding layout
     * associated with both the pipeline and the binding set.
     *
     * @param bindingSet The resource set to bind.
     */
    fun bindBindings(bindingSet: BindingSet)

    /**
     * Binds a vertex buffer to the specified input slot.
     *
     * The interpretation of the vertex data is determined by the vertex layout
     * declared by the currently bound graphics pipeline.
     *
     * @param slotIndex The zero-based vertex input slot.
     * @param buffer The vertex buffer to bind.
     */
    fun bindVertexBuffer(slotIndex: Int, buffer: GpuBuffer)

    /**
     * Binds an index buffer for indexed draw commands.
     *
     * @param buffer The index buffer to bind.
     */
    fun bindIndexBuffer(buffer: GpuBuffer)

    /**
     * Provides a small block of pass-local constant data.
     *
     * This function is intended for small frequently changing values. The backend
     * may implement it through push constants, uniform uploads, or another
     * suitable mechanism.
     *
     * @param data The constant data to provide to the pass.
     */
    fun pushConstants(data: MemorySpan)

    /**
     * Issues a non-indexed draw command.
     *
     * @param vertexCount The number of vertices to draw.
     * @param firstVertex The zero-based index of the first vertex.
     */
    fun draw(vertexCount: Int, firstVertex: Int = 0)

    /**
     * Issues an indexed draw command.
     *
     * @param indexCount The number of indices to draw.
     * @param firstIndex The zero-based index of the first index.
     * @param vertexOffset The value added to each index before fetching vertices.
     */
    fun drawIndexed(
        indexCount: Int,
        firstIndex: Int = 0,
        vertexOffset: Int = 0
    )

    /**
     * Issues a non-indexed instanced draw command.
     *
     * @param vertexCount The number of vertices to draw per instance.
     * @param instanceCount The number of instances to draw.
     * @param firstVertex The zero-based index of the first vertex.
     * @param firstInstance The zero-based index of the first instance.
     */
    fun drawInstanced(
        vertexCount: Int,
        instanceCount: Int,
        firstVertex: Int = 0,
        firstInstance: Int = 0
    )

    /**
     * Issues an indexed instanced draw command.
     *
     * @param indexCount The number of indices to draw per instance.
     * @param instanceCount The number of instances to draw.
     * @param firstIndex The zero-based index of the first index.
     * @param vertexOffset The value added to each index before fetching vertices.
     * @param firstInstance The zero-based index of the first instance.
     */
    fun drawIndexedInstanced(
        indexCount: Int,
        instanceCount: Int,
        firstIndex: Int = 0,
        vertexOffset: Int = 0,
        firstInstance: Int = 0
    )
}