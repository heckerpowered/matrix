package heckerpowered.render.api

/**
 * Describes how vertex data is laid out in a vertex buffer.
 *
 * A vertex layout defines how individual vertex attributes are read from
 * a single vertex record, including the total vertex stride and the set
 * of declared attributes.
 */
data class VertexLayout(
    val strideBytes: Int,
    val attributes: List<VertexAttribute>
)