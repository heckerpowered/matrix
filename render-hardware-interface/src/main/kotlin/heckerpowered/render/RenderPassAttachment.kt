package heckerpowered.render

/**
 * Describes how a texture participates in a render pass as an attachment.
 *
 * A render pass attachment combines:
 * - the target texture
 * - the role the texture plays in the pass
 * - how the attachment is initialized
 * - whether the result must be preserved after the pass
 * - an optional clear value
 *
 * This type describes pass-local usage only. It does not define the permanent
 * identity or purpose of the underlying texture.
 */
data class RenderPassAttachment(
    val texture: GpuTexture,
    val role: AttachmentRole,
    val loadAction: LoadAction,
    val storeAction: StoreAction,
    val clearValue: AttachmentClearValue? = null
)