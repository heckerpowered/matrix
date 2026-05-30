package heckerpowered.render.api

import heckerpowered.render.GpuBuffer
import heckerpowered.render.GpuResource
import heckerpowered.render.RenderPassDesc
import heckerpowered.render.ResourceUsage

/**
 * Exposes commands that may be recorded into the current frame.
 *
 * A frame scope is a frame-local recording context that may be passed through
 * long rendering call chains. It does not own frame lifetime management.
 *
 * The outer integration layer decides when frame recording begins and ends.
 * Ordinary rendering systems only use this scope to append commands to the
 * current frame.
 */
interface FrameScope {
    /**
     * Uploads memory into a GPU buffer.
     *
     * The readable bytes described by sourceMemory are copied into buffer
     * starting at destinationOffsetBytes.
     *
     * The implementation must consume or copy the provided memory before this
     * call returns. The caller is not required to keep the source memory alive
     * after the call completes.
     *
     * @param buffer The destination GPU buffer.
     * @param sourceMemory The source memory view.
     * @param destinationOffsetBytes The destination byte offset within buffer.
     */
    fun uploadBuffer(
        buffer: GpuBuffer,
        sourceMemory: MemorySpan,
        destinationOffsetBytes: Long = 0
    )

    /**
     * Declares the intended next usage of a GPU resource.
     *
     * This call communicates high-level usage intent to the backend. The backend
     * may translate it into barriers, layout transitions, synchronization, or
     * no operation if no explicit action is required.
     *
     * @param resource The resource whose next usage is being declared.
     * @param usage The intended next usage.
     */
    fun transition(
        resource: GpuResource,
        usage: ResourceUsage
    )

    /**
     * Records a compute pass within a structured scope.
     *
     * The optional label is intended for debugging and profiling.
     *
     * @param label A human-readable pass label.
     * @param block The compute pass recording block.
     */
    fun computePass(
        label: String = "",
        block: ComputePassScope.() -> Unit
    )

    /**
     * Records a render pass within a structured scope.
     *
     * @param description The render pass description.
     * @param block The render pass recording block.
     */
    fun renderPass(
        description: RenderPassDesc,
        block: RenderPassScope.() -> Unit
    )
}