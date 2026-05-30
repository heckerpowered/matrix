package heckerpowered.render.api

/**
 * Describes a single binding slot within a binding layout.
 *
 * A binding slot defines:
 * - the binding index used by shaders and pipelines
 * - the kind of resource expected at that binding
 *
 * A binding slot does not contain an actual resource. It only describes the
 * structure of a resource binding layout.
 */
data class BindingSlot(
    val binding: Int,
    val type: BindingType
)
