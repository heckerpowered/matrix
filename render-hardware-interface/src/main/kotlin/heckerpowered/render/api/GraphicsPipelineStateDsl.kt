package heckerpowered.render.api

/**
 * Creates a GraphicsPipelineState from a DSL block.
 *
 * Commands declared in the block are recorded in declaration order. When
 * multiple commands affect the same aspect of pipeline state, later commands
 * override earlier ones.
 */
fun GraphicsPipelineState(
    block: GraphicsPipelineStateBuilder.() -> Unit
): GraphicsPipelineState {
    val builder = GraphicsPipelineStateBuilder()
    builder.block()
    return builder.build()
}