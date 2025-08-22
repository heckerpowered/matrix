/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 *
 * This file is released under the MIT License.
 * See the LICENSE file in the project root for more information.
 */

package heckerpowered.matrix.client.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface MouseButtonEvent {
    companion object {
        @JvmField
        val EVENT: Event<MouseButtonEvent> =
            EventFactory.createArrayBacked(MouseButtonEvent::class.java) { listeners ->
                MouseButtonEvent { button, action, mods ->
                    for (listener in listeners) {
                        if (!listener.onMouseButton(button, action, mods)) {
                            return@MouseButtonEvent false
                        }
                    }

                    return@MouseButtonEvent true
                }
            }
    }

    fun onMouseButton(button: Int, action: Int, mods: Int): Boolean
}