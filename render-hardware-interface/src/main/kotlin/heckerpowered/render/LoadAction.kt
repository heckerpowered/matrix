package heckerpowered.render

/**
 * Describes how an attachment should be treated at the beginning of a render pass.
 *
 * This value defines whether the existing content of the attachment should be kept,
 * cleared, or ignored before rendering begins.
 */
enum class LoadAction {
    /**
     * Preserve the existing attachment content and make it available to the pass.
     */
    Load,

    /**
     * Clear the attachment at the beginning of the pass.
     *
     * A compatible clear value is expected when this action is used.
     */
    Clear,

    /**
     * Ignore the previous attachment content.
     *
     * The pass does not require the existing content to be preserved.
     */
    None
}
