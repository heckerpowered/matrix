package heckerpowered.render

/**
 * Describes the role an attachment plays within a render pass.
 *
 * The role defines how the backend should interpret and bind the attachment.
 * It does not describe the business meaning of the texture. For example,
 * an albedo buffer and a normal buffer are both color attachments even though
 * they store different kinds of data.
 */
enum class AttachmentRole {
    /**
     * The attachment is used as a color render target.
     *
     * Multiple color attachments may exist in the same render pass.
     */
    Color,

    /**
     * The attachment is used as a depth render target.
     */
    Depth,

    /**
     * The attachment is used as a stencil render target.
     */
    Stencil,

    /**
     * The attachment is used as a combined depth-stencil render target.
     */
    DepthStencil
}