/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gl.Framebuffer

fun interface InitAttachmentCallback {
    companion object {
        @JvmField
        val EVENT: Event<InitAttachmentCallback> =
            EventFactory.createArrayBacked(InitAttachmentCallback::class.java) { listeners ->
                InitAttachmentCallback { framebuffer ->
                    for (listener in listeners) {
                        listener.onInitAttachment(framebuffer)
                    }
                }
            }
    }

    /**
     * Invoked when a framebuffer attachment is initialized and bound to the OpenGL context.
     *
     * This method is called during the setup phase of a framebuffer's attachment,
     * typically after the texture or renderbuffer has been created and bound, but
     * before any rendering operations are performed.
     *
     * @param framebuffer The framebuffer instance to which the attachment belongs.
     *                    The relevant attachment (e.g., color or depth target) is bound
     *                    to the current OpenGL target at the time of this call.
     *
     * Implementers can use this hook to perform additional setup on the attachment,
     * such as configuring mipmap levels, setting texture parameters, or clearing initial data.
     */
    fun onInitAttachment(framebuffer: Framebuffer)
}