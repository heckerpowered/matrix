/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import org.lwjgl.glfw.GLFW

object MatrixKeyBindings {
    // 26.2: KeyMapping categories are registered KeyMapping.Category records instead of translation-key strings.
    private val category = KeyMapping.Category.register(Matrix.identifier("matrix"))

    val useMagic: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.matrix.use_magic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F,
            category
        )
    )

    val nextMagic: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.matrix.next_magic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            category
        )
    )

    val previousMagic: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.matrix.previous_magic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            category
        )
    )

    val overclockMagic = KeyMapping(
        "key.matrix.overclock_magic",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N,
        category
    )

    val overclockMana = KeyMapping(
        "key.matrix.overclock_mana",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        category
    )

    fun onInitialize() {
        // KeyBindingHelper.registerKeyBinding(useMagic)
        // KeyBindingHelper.registerKeyBinding(nextMagic)
        // KeyBindingHelper.registerKeyBinding(previousMagic)
    }
}