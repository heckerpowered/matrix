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

fun interface KeyEvent {
    companion object {
        @JvmField
        val EVENT: Event<KeyEvent> =
            EventFactory.createArrayBacked(KeyEvent::class.java) { listeners ->
                KeyEvent { key, scancode, action, mods ->
                    for (listener in listeners) {
                        if (!listener.onKey(key, scancode, action, mods)) {
                            return@KeyEvent false
                        }
                    }

                    return@KeyEvent true
                }
            }
    }

    fun onKey(key: Int, scancode: Int, action: Int, mods: Int): Boolean
}