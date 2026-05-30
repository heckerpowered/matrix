package heckerpowered.render.api


/**
 * Collects graphics pipeline state commands for a GraphicsPipelineState DSL block.
 *
 * Commands are appended in declaration order. When multiple commands affect
 * the same aspect of pipeline state, later commands override earlier ones.
 */
class GraphicsPipelineStateBuilder {
    private val commands = mutableListOf<PipelineState>()

    /**
     * Appends a blend enable command.
     *
     * @param enabled Whether blending should be enabled.
     */
    fun blend(enabled: Boolean) {
        commands += BlendState(enabled)
    }

    /**
     * Appends a depth test state command.
     *
     * @param enabled Whether depth testing should be enabled.
     */
    fun depthTest(enabled: Boolean) {
        commands += DepthTestState(enabled)
    }

    internal fun build(): GraphicsPipelineState {
        return GraphicsPipelineState(commands.toList())
    }
}