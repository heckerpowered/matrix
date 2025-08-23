/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface FinishRenderCallback {
    companion object {
        @JvmField
        val EVENT: Event<FinishRenderCallback> =
            EventFactory.createArrayBacked(FinishRenderCallback::class.java) { listeners ->
                FinishRenderCallback {
                    for (listener in listeners) {
                        listener.onFinishRender()
                    }
                }
            }
    }

    fun onFinishRender()
}