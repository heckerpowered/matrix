package heckerpowered.render.api

/**
 * Identifies the data format of a single vertex attribute.
 *
 * Vertex attribute formats describe how many floating-point components are
 * read for one attribute at a given shader input location.
 */
enum class VertexAttributeFormat {

    /**
     * One floating-point component.
     */
    Float1,

    /**
     * Two floating-point components.
     */
    Float2,

    /**
     * Three floating-point components.
     */
    Float3,

    /**
     * Four floating-point components.
     */
    Float4
}