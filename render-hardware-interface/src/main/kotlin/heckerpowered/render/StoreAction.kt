package heckerpowered.render

/**
 * Describes how an attachment should be treated at the end of a render pass.
 *
 * This value defines whether the result written by the pass must be preserved
 * after the pass completes.
 */
enum class StoreAction {
    /**
     * Preserve the attachment content produced by the pass.
     */
    Store,

    /**
     * The attachment content does not need to be preserved after the pass ends.
     */
    None
}