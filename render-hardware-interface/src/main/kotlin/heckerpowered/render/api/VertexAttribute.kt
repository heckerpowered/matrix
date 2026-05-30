package heckerpowered.render.api

/**
 * Describes a single vertex attribute within a vertex layout.
 *
 * A vertex attribute defines:
 * - the shader input location it maps to
 * - the attribute data format
 * - the byte offset within a single vertex record
 */
data class VertexAttribute(
    val location: Int,
    val format: VertexAttributeFormat,
    val offsetBytes: Int
)