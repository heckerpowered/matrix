package heckerpowered.render

import java.awt.Color

/**
 * Represents a clear value compatible with a render pass attachment.
 *
 * The concrete value type must match the role and format expectations of the
 * attachment it is used with.
 */
sealed interface AttachmentClearValue

/**
 * Clear value for a color attachment.
 */
data class ColorAttachmentClearValue(val red: Float, val green: Float, val blue: Float, val alpha: Float) : AttachmentClearValue

/**
 * Clear value for a depth attachment.
 */
data class DepthAttachmentClearValue(val depth: Float) : AttachmentClearValue

/**
 * Clear value for a depth-stencil attachment.
 */
data class DepthStencilAttachmentClearValue(val depth: Float, val stencil: Float) : AttachmentClearValue