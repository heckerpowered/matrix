package heckerpowered.render.api


/**
 * Exposes commands that may be recorded within a compute pass scope.
 *
 * A compute pass scope is valid only for the duration of the block passed to
 * CommandRecorder.computePass. It is not responsible for pass lifetime
 * management. The recorder implementation opens and closes the underlying pass.
 */
interface ComputePassScope {
    /**
     * Binds the compute pipeline used by subsequent dispatch commands in this scope.
     *
     * @param pipeline The compute pipeline to bind.
     */
    fun bindPipeline(pipeline: ComputePipeline)

    /**
     * Binds a resource set used by the currently bound compute pipeline.
     *
     * The meaning of each bound resource is determined by the binding layout
     * associated with both the pipeline and the binding set.
     *
     * @param bindingSet The resource set to bind.
     */
    fun bindBindings(bindingSet: BindingSet)

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
     * Dispatches compute workgroups.
     *
     * The dispatch dimensions are expressed in workgroup counts, not in thread
     * counts. The effective total invocation count depends on the local workgroup
     * size declared by the compute shader.
     *
     * @param groupCountX The number of workgroups to dispatch in the X dimension.
     * @param groupCountY The number of workgroups to dispatch in the Y dimension.
     * @param groupCountZ The number of workgroups to dispatch in the Z dimension.
     */
    fun dispatch(groupCountX: Int, groupCountY: Int, groupCountZ: Int)
}