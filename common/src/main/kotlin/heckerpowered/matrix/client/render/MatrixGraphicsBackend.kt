/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

import com.mojang.blaze3d.systems.RenderSystem

object MatrixGraphicsBackend {
    private val deviceClassName: String?
        get() = RenderSystem.tryGetDevice()?.javaClass?.name

    fun isVulkan(): Boolean {
        return deviceClassName?.contains("vulkan", ignoreCase = true) == true
    }

    fun isOpenGl(): Boolean {
        val name = deviceClassName ?: return false
        return name.contains("opengl", ignoreCase = true) || name.contains(".Gl", ignoreCase = false)
    }
}
