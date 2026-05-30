package heckerpowered.render.api

/**
 * Describes the structure of a resource binding set.
 *
 * A binding layout defines which binding slots exist and what kind of resource
 * each slot expects. It acts as the contract between shader resource declarations,
 * pipeline expectations, and concrete binding set instances.
 *
 * A binding layout does not contain actual GPU resources. It only describes the
 * shape of a resource set.
 */
interface BindingLayout