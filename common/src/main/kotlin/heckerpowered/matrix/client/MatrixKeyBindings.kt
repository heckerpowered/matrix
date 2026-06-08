/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client

import com.mojang.blaze3d.platform.InputConstants
import heckerpowered.matrix.Matrix
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object MatrixKeyBindings {
    private val category = KeyMapping.Category.register(Matrix.identifier("key_category"))

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

    val overclockMagic: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.matrix.overclock_magic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            category
        )
    )

    val overclockMana: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.matrix.overclock_mana",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            category
        )
    )

    fun onInitialize() {
        // Key mappings are registered during property initialization.
    }
}
