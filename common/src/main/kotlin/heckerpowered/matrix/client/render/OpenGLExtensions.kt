/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 heckerpowered
 */

package heckerpowered.matrix.client.render

object OpenGLExtensions {
    @JvmStatic fun clearGLError() = Unit
    @JvmStatic fun checkGLError(handler: (Int) -> Unit) = Unit
    @JvmStatic fun getErrorName(error: Int): String = "GL_ERROR_$error"
    @JvmStatic fun getErrorDescription(error: Int): String = getErrorName(error)
    @JvmStatic fun fastCheck(name: String) = Unit
    @JvmStatic fun getFramebufferStatusName(status: Int): String = "GL_FRAMEBUFFER_STATUS_$status"
    @JvmStatic fun getFramebufferStatusDescription(status: Int): String = getFramebufferStatusName(status)
    @JvmStatic fun getPackedPixelDataTypeForFormat(format: Int): Int = 0
    @JvmStatic fun getBytesPerPixel(format: Int, type: Int): Int = 4
    @JvmStatic fun initGLContext(name: String = "") = Unit
}
