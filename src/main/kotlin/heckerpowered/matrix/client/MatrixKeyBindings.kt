/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2025 heckerpowered
 */

package heckerpowered.matrix.client

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object MatrixKeyBindings {
    val useMagic: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.matrix.use_magic",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            "key.categories.matrix"
        )
    )

    val nextMagic: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.matrix.next_magic",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "key.categories.matrix"
        )
    )

    val previousMagic: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.matrix.previous_magic",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "key.categories.matrix"
        )
    )

    val overclockMagic = KeyBinding(
        "key.matrix.overclock_magic",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        "key.categories.matrix"
    )

    val overclockMana = KeyBinding(
        "key.matrix.overclock_mana",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        "key.categories.matrix"
    )

    fun onInitialize() {
        // KeyBindingHelper.registerKeyBinding(useMagic)
        // KeyBindingHelper.registerKeyBinding(nextMagic)
        // KeyBindingHelper.registerKeyBinding(previousMagic)
    }
}