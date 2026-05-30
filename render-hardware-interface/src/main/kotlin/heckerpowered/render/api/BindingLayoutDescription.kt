package heckerpowered.render.api

/**
 * Describes the structure of a binding layout before it is created by a device.
 *
 * A binding layout description defines which binding slots exist and what kind
 * of resource each slot expects. It is pure description data and does not
 * contain backend state or actual GPU resources.
 *
 * This type is used as input to device-side layout creation.
 */
data class BindingLayoutDescription(
    val bindings: List<BindingSlot>
)
